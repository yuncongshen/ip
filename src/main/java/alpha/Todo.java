package alpha;

/**
 * Represents a ToDo task, which has only a description and no deadline or
 * event times.
 */
public class Todo extends Task {

    /**
     * Creates a new ToDo task with the given description.
     *
     * @param description The text describing the task.
     */
    public Todo(String description) {
        super(description);
    }

    /**
     * Returns a string representation of this task in the form
     * {@code [T][ ] description}.
     *
     * @return The string representation.
     */
    @Override
    public String toString() {
        return "[T]" + super.toString();
    }
}
