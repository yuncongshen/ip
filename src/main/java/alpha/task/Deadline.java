package alpha.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

/**
 * Represents a Deadline task, which has a description and a deadline.
 */
public class Deadline extends Task {
    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("MMM dd uuuu", Locale.ENGLISH);
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("MMM dd uuuu, HH:mm",
            Locale.ENGLISH);
    private static final DateTimeFormatter INPUT_DATE = DateTimeFormatter.ofPattern("d/M/uuuu", Locale.ENGLISH)
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter INPUT_TIME = DateTimeFormatter.ofPattern("d/M/uuuu HHmm", Locale.ENGLISH)
            .withResolverStyle(ResolverStyle.STRICT);
    private static final String INVALID_DATE = "Invalid deadline; use yyyy-MM-dd or d/M/yyyy HHmm"
            + " (e.g., 2/12/2019 1800)";
    private final LocalDateTime by;
    // Distinguishes an unspecified time from an explicit midnight deadline.
    private final boolean hasTime;

    /**
     * Creates a new Deadline task with the given description and deadline.
     *
     * @param description The text describing the task.
     * @param by An ISO date, a day-first slash date with optional HHmm or HH:mm time, or a saved ISO datetime.
     * @throws IllegalArgumentException If the date or time is missing, malformed, or impossible.
     */
    public Deadline(String description, String by) {
        super(description);
        try {
            if (by != null && by.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
                this.by = LocalDate.parse(by).atStartOfDay();
                hasTime = false;
            } else if (by != null && by.matches("[0-9]{1,2}/[0-9]{1,2}/[0-9]{4}")) {
                this.by = LocalDate.parse(by, INPUT_DATE).atStartOfDay();
                hasTime = false;
            } else if (by != null && by.matches("[0-9]{1,2}/[0-9]{1,2}/[0-9]{4} [0-9]{2}:?[0-9]{2}")) {
                this.by = LocalDateTime.parse(by.replace(":", ""), INPUT_TIME);
                hasTime = true;
            } else if (by != null && by.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}")) {
                this.by = LocalDateTime.parse(by);
                hasTime = true;
            } else {
                throw new IllegalArgumentException(INVALID_DATE);
            }
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(INVALID_DATE, exception);
        }
    }

    public LocalDateTime getBy() {
        return by;
    }

    public boolean hasTime() {
        return hasTime;
    }

    /**
     * Returns an ISO date or datetime for storage, preserving whether a time was supplied.
     */
    public String toStorageString() {
        return hasTime ? by.toString() : by.toLocalDate().toString();
    }

    /**
     * Returns a string representation of this task in the form
     * {@code [D][ ] description (by: Oct 15 2019)}.
     *
     * @return The string representation.
     */
    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + by.format(hasTime ? DISPLAY_TIME : DISPLAY_DATE) + ")";
    }
}
