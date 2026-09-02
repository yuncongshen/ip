package alpha;

/**
 * Represents a Deadline task, which has a description and a deadline.
 */
public class Deadline extends Task {
    protected String by;

    /**
     * Creates a new Deadline task with the given description and deadline.
     *
     * @param description The text describing the task.
     * @param by The deadline datetime.
     */
    public Deadline(String description, String by) {
        super(description);
        this.by = by;
    }

    /**
     * Returns a string representation of this task in the form
     * {@code [D][ ] description (by: datetime)}.
     *
     * @return The string representation.
     */
    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + by + ")";
    }
}
