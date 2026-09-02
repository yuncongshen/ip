package alpha;

/**
 * Represents a task in the Alpha chatbot application.
 * Each task has a description, a type (ToDo, Deadline, or Event),
 * optional date/time fields, and a done status that can be toggled.
 */
public class Task {
    private String description;
    private boolean isDone;
    private String type;
    private String by;
    private String from;
    private String to;

    /**
     * Creates a new task with the given description, type, and optional time fields.
     * A new task is not done by default.
     *
     * @param description The text describing the task.
     * @param type The task type: "T" for ToDo, "D" for Deadline, "E" for Event.
     * @param by The deadline datetime, or {@code null} for non-deadline tasks.
     * @param from The event start datetime, or {@code null} for non-event tasks.
     * @param to The event end datetime, or {@code null} for non-event tasks.
     */
    public Task(String description, String type, String by, String from, String to) {
        this.description = description;
        this.isDone = false;
        this.type = type;
        this.by = by;
        this.from = from;
        this.to = to;
    }

    /**
     * Creates a ToDo task with the given description.
     *
     * @param description The text describing the task.
     * @return A new ToDo task.
     */
    public static Task createTodo(String description) {
        return new Task(description, "T", null, null, null);
    }

    /**
     * Creates a Deadline task with the given description and deadline.
     *
     * @param description The text describing the task.
     * @param by The deadline datetime.
     * @return A new Deadline task.
     */
    public static Task createDeadline(String description, String by) {
        return new Task(description, "D", by, null, null);
    }

    /**
     * Creates an Event task with the given description, start, and end datetimes.
     *
     * @param description The text describing the task.
     * @param from The event start datetime.
     * @param to The event end datetime.
     * @return A new Event task.
     */
    public static Task createEvent(String description, String from, String to) {
        return new Task(description, "E", null, from, to);
    }

    /**
     * Returns the status icon for this task: "X" if done, or a space if not done.
     *
     * @return The status icon.
     */
    public String getStatusIcon() {
        return isDone ? "X" : " ";
    }

    /**
     * Returns the description of this task.
     *
     * @return The task description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Marks this task as done.
     */
    public void markAsDone() {
        this.isDone = true;
    }

    /**
     * Marks this task as not done.
     */
    public void markAsNotDone() {
        this.isDone = false;
    }

    /**
     * Returns a string representation of this task including its type and status.
     * ToDo tasks are shown as "[T][X] desc".
     * Deadline tasks are shown as "[D][ ] desc (by: datetime)".
     * Event tasks are shown as "[E][ ] desc (from: start to: end)".
     *
     * @return The string representation.
     */
    @Override
    public String toString() {
        String base = "[" + type + "][" + getStatusIcon() + "] " + description;
        if (type.equals("D")) {
            return base + " (by: " + by + ")";
        } else if (type.equals("E")) {
            return base + " (from: " + from + " to: " + to + ")";
        }
        return base;
    }
}
