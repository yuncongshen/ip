package alpha;

import alpha.task.Deadline;
import alpha.task.Event;
import alpha.task.Task;
import alpha.task.Todo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads validated snapshots and replaces them atomically without discarding failed or conflicting saves.
 */
public class Storage {
    private static final String HEADER = "ALPHA-1";
    private static final int MAX_FILE_BYTES = 1_048_576;
    private final Path file;
    private final Path directory;
    private final String displayPath;
    // Retains the last observed bytes to detect edits by another session before saving.
    private byte[] savedBytes;

    /**
     * Creates storage for the given snapshot path.
     *
     * @param file The file to read and write.
     */
    public Storage(Path file) {
        this.file = file.normalize();
        this.directory = this.file.getParent() == null ? Path.of(".") : this.file.getParent();
        this.displayPath = this.file.toString().replace(this.file.getFileSystem().getSeparator(), "/");
    }

    /**
     * Loads all tasks, preserving the destination list if any record is invalid.
     *
     * @param tasks The destination list.
     * @return The number of restored tasks.
     * @throws AlphaException If the snapshot cannot be read or validated.
     */
    public int load(ArrayList<Task> tasks) throws AlphaException {
        try {
            byte[] bytes = readSnapshot();
            if (bytes == null) {
                tasks.clear();
                savedBytes = null;
                return 0;
            }
            String text = decodeUtf8(bytes);
            if (text.startsWith("\uFEFF")) {
                text = text.substring(1);
            }
            String[] lines = text.split("\\r\\n|\\n|\\r", -1);
            int firstLine = 0;
            while (firstLine < lines.length && lines[firstLine].isBlank()) {
                firstLine++;
            }
            boolean isVersioned = firstLine < lines.length && lines[firstLine].equals(HEADER);
            ArrayList<Task> restored = new ArrayList<>();
            for (int i = isVersioned ? firstLine + 1 : firstLine; i < lines.length; i++) {
                if (lines[i].isBlank()) {
                    continue;
                }
                try {
                    restored.add(isVersioned ? parseRecord(lines[i]) : parseLegacyRecord(lines[i]));
                } catch (IllegalArgumentException exception) {
                    throw new IOException("Invalid task at line " + (i + 1) + ": " + exception.getMessage(),
                            exception);
                }
            }
            tasks.clear();
            tasks.addAll(restored);
            savedBytes = bytes;
            return restored.size();
        } catch (CharacterCodingException exception) {
            throw new AlphaException("Cannot load " + displayPath + ": invalid UTF-8. The file has not been changed.");
        } catch (IOException | SecurityException | UnsupportedOperationException exception) {
            throw new AlphaException("Cannot load " + displayPath + ": " + exception.getMessage()
                    + ". The file has not been changed.");
        }
    }

    /**
     * Writes a complete snapshot and atomically replaces the previous file.
     *
     * @param tasks The task list.
     * @throws AlphaException If the save fails or conflicts with an external edit.
     */
    public void save(ArrayList<Task> tasks) throws AlphaException {
        Path temporary = null;
        boolean isCommitted = false;
        try {
            StringBuilder text = new StringBuilder(HEADER).append('\n');
            for (int i = 0; i < tasks.size(); i++) {
                if (tasks.get(i) == null) {
                    throw new IllegalArgumentException("Missing task at position " + (i + 1));
                }
                text.append(formatRecord(tasks.get(i))).append('\n');
            }
            byte[] bytes = text.toString().getBytes(StandardCharsets.UTF_8);
            if (bytes.length > MAX_FILE_BYTES) {
                throw new IOException("The snapshot exceeds the 1 MiB size limit");
            }
            Files.createDirectories(directory);
            Path lockFile = file.resolveSibling(".alpha.lock");
            try (FileChannel channel = FileChannel.open(lockFile, StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE, LinkOption.NOFOLLOW_LINKS);
                    FileLock lock = channel.tryLock()) {
                if (lock == null) {
                    throw new IOException("Another Alpha session is saving; try again");
                }
                verifyUnchanged();
                temporary = Files.createTempFile(directory, ".alpha-", ".tmp");
                try (FileChannel output = FileChannel.open(temporary, StandardOpenOption.WRITE)) {
                    ByteBuffer buffer = ByteBuffer.wrap(bytes);
                    while (buffer.hasRemaining()) {
                        output.write(buffer);
                    }
                    output.force(true);
                }
                verifyUnchanged();
                Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                savedBytes = bytes;
                isCommitted = true;
            }
        } catch (AtomicMoveNotSupportedException exception) {
            throw new AlphaException("This filesystem cannot safely replace the save file. Change not applied.");
        } catch (IOException | SecurityException | IllegalArgumentException | OverlappingFileLockException
                | UnsupportedOperationException exception) {
            if (isCommitted) {
                System.err.println("Tasks were saved, but closing the save lock failed: " + exception.getMessage());
                return;
            }
            throw new AlphaException("Cannot save " + displayPath + ": " + exception.getMessage()
                    + ". Change not applied.");
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException | SecurityException exception) {
                    System.err.println("Could not remove temporary save file " + temporary + ": "
                            + exception.getMessage());
                }
            }
        }
    }

    /**
     * Rejects external changes instead of silently replacing a newer snapshot.
     */
    private void verifyUnchanged() throws IOException {
        if (!Arrays.equals(savedBytes, readSnapshot())) {
            throw new IOException("The save file changed outside this session; restart Alpha to reload it");
        }
    }

    /**
     * Reads a bounded regular file, distinguishing a missing file from inaccessible or unsafe paths.
     */
    private byte[] readSnapshot() throws IOException {
        BasicFileAttributes attributes;
        try {
            attributes = Files.readAttributes(file, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        } catch (NoSuchFileException exception) {
            if (Files.notExists(file, LinkOption.NOFOLLOW_LINKS)) {
                return null;
            }
            throw exception;
        }
        if (!attributes.isRegularFile()) {
            throw new IOException("The save path must be a regular file, not a directory or symbolic link");
        }
        try (InputStream input = Files.newInputStream(file, LinkOption.NOFOLLOW_LINKS)) {
            byte[] bytes = input.readNBytes(MAX_FILE_BYTES + 1);
            if (bytes.length > MAX_FILE_BYTES) {
                throw new IOException("The save file exceeds the 1 MiB size limit");
            }
            return bytes;
        }
    }

    /**
     * Decodes UTF-8 without silently replacing malformed bytes.
     */
    private static String decodeUtf8(byte[] bytes) throws CharacterCodingException {
        return StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
    }

    /**
     * Encodes fields separately so punctuation and line breaks cannot be mistaken for structure.
     */
    private static String formatRecord(Task task) {
        if (task.getDescription().isBlank()) {
            throw new IllegalArgumentException("Missing task description");
        }
        String status = task.getStatusIcon().equals("X") ? "1" : "0";
        String body = status + "|" + encode(task.getDescription());
        if (task instanceof Deadline deadline) {
            return "D|" + body + "|" + encode(deadline.getBy());
        } else if (task instanceof Event event) {
            return "E|" + body + "|" + encode(event.getFrom()) + "|" + encode(event.getTo());
        } else if (task instanceof Todo) {
            return "T|" + body;
        }
        throw new IllegalArgumentException("Unsupported task type");
    }

    private static String encode(String field) {
        return Base64.getEncoder().encodeToString(field.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String field) {
        try {
            return decodeUtf8(Base64.getDecoder().decode(field));
        } catch (CharacterCodingException exception) {
            throw new IllegalArgumentException("A saved field is not valid UTF-8", exception);
        }
    }

    /**
     * Validates the record shape, completion status, and description before constructing a task.
     */
    private static Task parseRecord(String line) {
        String[] fields = line.split("\\|", -1);
        if (fields.length < 3 || !(fields[1].equals("0") || fields[1].equals("1"))) {
            throw new IllegalArgumentException("Invalid field count or completion status");
        }
        String description = decode(fields[2]);
        if (description.isBlank()) {
            throw new IllegalArgumentException("Missing task description");
        }
        Task task;
        if (fields[0].equals("T") && fields.length == 3) {
            task = new Todo(description);
        } else if (fields[0].equals("D") && fields.length == 4) {
            task = new Deadline(description, decode(fields[3]));
        } else if (fields[0].equals("E") && fields.length == 5) {
            task = new Event(description, decode(fields[3]), decode(fields[4]));
        } else {
            throw new IllegalArgumentException("Unknown task type or incorrect field count");
        }
        if (fields[1].equals("1")) {
            task.markAsDone();
        }
        return task;
    }

    /**
     * Imports earlier display-format snapshots, rejecting ambiguous separators rather than guessing.
     */
    private static Task parseLegacyRecord(String line) {
        Matcher fields = Pattern.compile("\\[([TDE])\\]\\[([ X])\\] (.*)").matcher(line);
        if (!fields.matches()) {
            throw new IllegalArgumentException("Unknown file version or invalid legacy record");
        }
        String body = fields.group(3);
        String record = fields.group(1) + "|" + (fields.group(2).equals("X") ? "1" : "0") + "|";
        if (fields.group(1).equals("T")) {
            return parseRecord(record + encode(body));
        }
        String marker = fields.group(1).equals("D") ? " (by: " : " (from: ";
        int boundary = body.indexOf(marker);
        if (boundary < 0 || boundary != body.lastIndexOf(marker) || !body.endsWith(")")) {
            throw new IllegalArgumentException("Missing or ambiguous legacy time fields");
        }
        record += encode(body.substring(0, boundary)) + "|";
        String times = body.substring(boundary + marker.length(), body.length() - 1);
        if (fields.group(1).equals("D")) {
            return parseRecord(record + encode(times));
        }
        int separator = times.indexOf(" to: ");
        if (separator < 0 || separator != times.lastIndexOf(" to: ")) {
            throw new IllegalArgumentException("Missing or ambiguous legacy event times");
        }
        return parseRecord(record + encode(times.substring(0, separator)) + "|"
                + encode(times.substring(separator + " to: ".length())));
    }
}
