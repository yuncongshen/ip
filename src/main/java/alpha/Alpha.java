package alpha;

import alpha.task.Deadline;
import alpha.task.Event;
import alpha.task.Task;
import alpha.task.Todo;

import java.nio.file.Path;
import java.util.ArrayList;

/**
 * Runs the Alpha chatbot command-line application.
 */
public class Alpha {
    private final Ui ui = new Ui();
    private final Storage storage = new Storage(Path.of("data", "alpha.txt"));

    /**
     * Greets the user, restores saved tasks, manages them, and exits on {@code bye}.
     * Supported commands are {@code todo}, {@code deadline}, {@code event},
     * {@code list}, {@code mark}, {@code unmark}, {@code delete}, and {@code bye}.
     *
     * @param args Command-line arguments, which are not used.
     */
    public static void main(String[] args) {
        new Alpha().run();
    }

    /**
     * Restores tasks and handles commands until exit, end of input, or a loading error.
     */
    public void run() {
        ui.showWelcome();

        ArrayList<Task> tasks = new ArrayList<>();
        try {
            storage.load(tasks);
        } catch (AlphaException exception) {
            ui.showError(exception.getMessage());
            ui.showDivider();
            return;
        }
        while (ui.hasNextCommand()) {
            String command = ui.readCommand();

            if (command.equals("bye")) {
                ui.showGoodbye();
                ui.showDivider();
                break;
            }

            try {
                processCommand(command, tasks);
            } catch (AlphaException exception) {
                ui.showError(exception.getMessage());
            }
            ui.showDivider();
        }
    }

    /**
     * Processes a non-exit command and updates the task list.
     *
     * @param command The user's command.
     * @param tasks The list of tasks.
     * @throws AlphaException If the command is invalid or its change cannot be saved.
     */
    private void processCommand(String command, ArrayList<Task> tasks)
            throws AlphaException {
        if (command.equals("list")) {
            ui.showTasks(tasks);
        } else if (command.equals("mark") || command.startsWith("mark ")) {
            markTask(command, tasks);
        } else if (command.equals("unmark") || command.startsWith("unmark ")) {
            unmarkTask(command, tasks);
        } else if (command.equals("delete") || command.startsWith("delete ")) {
            deleteTask(command, tasks);
        } else if (command.equals("todo") || command.startsWith("todo ")) {
            addTodo(command, tasks);
        } else if (command.equals("deadline") || command.startsWith("deadline ")) {
            addDeadline(command, tasks);
        } else if (command.equals("event") || command.startsWith("event ")) {
            addEvent(command, tasks);
        } else {
            throw new AlphaException("Bro, I don't know what that means...");
        }
    }

    /**
     * Marks the task at the one-based index in the given command as done.
     *
     * @param command The user's input, for example, "mark 2".
     * @param tasks The list of tasks.
     * @throws AlphaException If the task number is invalid or its change cannot be saved.
     */
    private void markTask(String command, ArrayList<Task> tasks)
            throws AlphaException {
        int index = parseIndex(command, "mark".length(), tasks.size());

        boolean wasDone = tasks.get(index).getStatusIcon().equals("X");
        tasks.get(index).markAsDone();
        try {
            storage.save(tasks);
        } catch (AlphaException exception) {
            if (!wasDone) {
                tasks.get(index).markAsNotDone();
            }
            throw exception;
        }
        ui.showMarked(tasks.get(index));
    }

    /**
     * Marks the task at the one-based index in the given command as not done.
     *
     * @param command The user's input, for example, "unmark 2".
     * @param tasks The list of tasks.
     * @throws AlphaException If the task number is invalid or its change cannot be saved.
     */
    private void unmarkTask(String command, ArrayList<Task> tasks)
            throws AlphaException {
        int index = parseIndex(command, "unmark".length(), tasks.size());

        boolean wasDone = tasks.get(index).getStatusIcon().equals("X");
        tasks.get(index).markAsNotDone();
        try {
            storage.save(tasks);
        } catch (AlphaException exception) {
            if (wasDone) {
                tasks.get(index).markAsDone();
            }
            throw exception;
        }
        ui.showUnmarked(tasks.get(index));
    }

    /**
     * Deletes a task while preserving the order of the remaining tasks.
     *
     * @param command The user's input, for example, "delete 3".
     * @param tasks The list of tasks.
     * @throws AlphaException If the task number is missing, invalid, or deletion cannot be saved.
     */
    private void deleteTask(String command, ArrayList<Task> tasks)
            throws AlphaException {
        int index = parseIndex(command, "delete".length(), tasks.size());
        Task deletedTask = tasks.remove(index);
        try {
            storage.save(tasks);
        } catch (AlphaException exception) {
            tasks.add(index, deletedTask);
            throw exception;
        }

        ui.showDeleted(deletedTask, tasks.size());
    }

    /**
     * Parses the task number from the given command into a zero-based index.
     *
     * @param command The user's input, for example, "mark 2".
     * @param prefixLength The length of the command prefix, for example, "mark ".
     * @param taskCount The number of tasks stored.
     * @return The zero-based task index.
     * @throws AlphaException If the number is not a valid positive integer
     *                        or does not refer to a stored task.
     */
    private static int parseIndex(String command, int prefixLength, int taskCount)
            throws AlphaException {
        int taskNumber;
        try {
            taskNumber = Integer.parseInt(command.substring(prefixLength).trim());
        } catch (NumberFormatException exception) {
            throw new AlphaException("Please give me a task number, e.g. \"mark 2\"...");
        }

        if (taskNumber <= 0 || taskNumber > taskCount) {
            throw new AlphaException("There is no task with that number...");
        }

        return taskNumber - 1;
    }

    /**
     * Adds a ToDo task described in the given command to the task list.
     *
     * @param command The user's input, for example, "todo borrow book".
     * @param tasks The list of tasks.
     * @throws AlphaException If the description is empty.
     */
    private void addTodo(String command, ArrayList<Task> tasks)
            throws AlphaException {
        String description = command.length() > "todo ".length()
                ? command.substring("todo ".length()).trim()
                : "";
        if (description.isEmpty()) {
            throw new AlphaException("Bro, please add a description...");
        }
        tasks.add(new Todo(description));
        saveAddition(tasks);
    }

    /**
     * Adds a Deadline task described in the given command to the task list.
     * The description and deadline are separated by the "/by" marker.
     *
     * @param command The user's input, for example, "deadline return book /by Sunday".
     * @param tasks The list of tasks.
     * @throws AlphaException If the description is empty.
     */
    private void addDeadline(String command, ArrayList<Task> tasks)
            throws AlphaException {
        String body = command.length() > "deadline ".length()
                ? command.substring("deadline ".length()).trim()
                : "";
        String[] parts = body.split(" /by ", 2);
        String description = parts[0];
        if (description.isEmpty()) {
            throw new AlphaException("Bro, please add a description...");
        }
        String by = parts.length > 1 ? parts[1] : "";
        tasks.add(new Deadline(description, by));
        saveAddition(tasks);
    }

    /**
     * Adds an Event task described in the given command to the task list.
     * The description and start/end datetimes are separated by the "/from" and "/to" markers.
     *
     * @param command The user's input, for example, "event meeting /from Mon 2pm /to 4pm".
     * @param tasks The list of tasks.
     * @throws AlphaException If the description is empty.
     */
    private void addEvent(String command, ArrayList<Task> tasks)
            throws AlphaException {
        String body = command.length() > "event ".length()
                ? command.substring("event ".length()).trim()
                : "";
        String[] parts = body.split(" /from ", 2);
        String description = parts[0];
        if (description.isEmpty()) {
            throw new AlphaException("Bro, please add a description...");
        }
        String[] times = parts.length > 1 ? parts[1].split(" /to ", 2) : new String[] {"", ""};
        String from = times[0];
        String to = times.length > 1 ? times[1] : "";
        tasks.add(new Event(description, from, to));
        saveAddition(tasks);
    }

    /**
     * Saves an addition before confirming it, removing the new task if saving fails.
     *
     * @param tasks The list of tasks.
     * @throws AlphaException If the addition cannot be saved.
     */
    private void saveAddition(ArrayList<Task> tasks) throws AlphaException {
        try {
            storage.save(tasks);
        } catch (AlphaException exception) {
            tasks.remove(tasks.size() - 1);
            throw exception;
        }
        ui.showAdded(tasks.get(tasks.size() - 1), tasks.size());
    }
}
