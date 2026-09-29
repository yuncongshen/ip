package alpha.task;

/**
 * Represents an event with a description and free-text start and end fields.
 * Unlike deadline dates, event times are not parsed into calendar values.
 */
public class Event extends Task {
    /**
     * Stores the event's start text without interpreting it as a datetime.
     */
    protected String from;
    /**
     * Stores the event's end text without interpreting it as a datetime.
     */
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
     * Returns the event's start text, or an empty string when no start was supplied.
     */
    public String getFrom() {
        return from;
    }

    /**
     * Returns the event's end text, or an empty string when no end was supplied.
     */
    public String getTo() {
        return to;
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
