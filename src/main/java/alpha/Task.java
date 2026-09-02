package alpha;

/**
 * Represents the common behaviour shared by all task types in the Alpha
 * chatbot application. Each task has a description and a done status that
 * can be toggled. Subclasses {@code Todo}, {@code Deadline}, and
 * {@code Event} add their own type-specific details.
 */
public class Task {
    protected String description;
    private boolean isDone;

    /**
     * Creates a new task with the given description.
     * A new task is not done by default.
     *
     * @param description The text describing the task.
     */
    public Task(String description) {
        this.description = description;
        this.isDone = false;
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
     * Returns the common part of this task's string representation, including
     * its status and description. Subclasses prepend their type marker and
     * append their type-specific details.
     *
     * @return The string representation of the status and description.
     */
    @Override
    public String toString() {
        return "[" + getStatusIcon() + "] " + description;
    }
}
