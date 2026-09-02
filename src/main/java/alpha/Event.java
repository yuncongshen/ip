package alpha;

/**
 * Represents an Event task, which has a description and start and end
 * datetimes.
 */
public class Event extends Task {
    protected String from;
    protected String to;

    /**
     * Creates a new Event task with the given description, start, and end
     * datetimes.
     *
     * @param description The text describing the task.
     * @param from The event start datetime.
     * @param to The event end datetime.
     */
    public Event(String description, String from, String to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    /**
     * Returns a string representation of this task in the form
     * {@code [E][ ] description (from: start to: end)}.
     *
     * @return The string representation.
     */
    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + from + " to: " + to + ")";
    }
}
