/**
 * Represents a task in the Alpha chatbot application.
 * Each task has a description and a done status that can be toggled.
 */
public class Task {
    protected String description;
    protected boolean isDone;

    /**
     * Creates a new task with the given description. A new task is not done by default.
     *
     * @param description the text describing the task
     */
    public Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    /**
     * Returns the status icon for this task: "X" if done, " " (space) if not done.
     *
     * @return the status icon
     */
    public String getStatusIcon() {
        return (isDone ? "X" : " ");
    }

    /**
     * Returns the description of this task.
     *
     * @return the task description
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
     * Returns a string representation of this task with its status icon,
     * e.g. "[X] read book".
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "[" + getStatusIcon() + "] " + description;
    }
}
