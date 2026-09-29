package alpha;

import alpha.task.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Owns the ordered task collection and its in-memory operations, independently of storage and display.
 * Indices are zero-based and must refer to valid positions, as checked by the parser for user commands.
 */
public class TaskList {
    private final ArrayList<Task> tasks;

    /**
     * Creates an empty task list.
     */
    public TaskList() {
        tasks = new ArrayList<>();
    }

    /**
     * Creates a list with its own collection containing the supplied tasks in order.
     *
     * @param initialTasks The tasks restored from storage.
     */
    public TaskList(List<Task> initialTasks) {
        tasks = new ArrayList<>(initialTasks);
    }

    public int size() {
        return tasks.size();
    }

    public Task get(int index) {
        return tasks.get(index);
    }

    /**
     * Appends a task to the list.
     *
     * @param task The task to add.
     */
    public void add(Task task) {
        tasks.add(task);
    }

    /**
     * Inserts a task at its former position when undoing a failed deletion.
     *
     * @param index The position to restore, from zero through the current size.
     * @param task The deleted task.
     */
    public void insert(int index, Task task) {
        tasks.add(index, task);
    }

    /**
     * Removes and returns a task, preserving the order of the remaining tasks.
     *
     * @param index The validated task index.
     */
    public Task delete(int index) {
        return tasks.remove(index);
    }

    /**
     * Returns whether the task at the given index is done.
     *
     * @param index The validated task index.
     */
    public boolean isDone(int index) {
        return tasks.get(index).getStatusIcon().equals("X");
    }

    /**
     * Marks a task as done.
     *
     * @param index The validated task index.
     */
    public void mark(int index) {
        tasks.get(index).markAsDone();
    }

    /**
     * Marks a task as not done.
     *
     * @param index The validated task index.
     */
    public void unmark(int index) {
        tasks.get(index).markAsNotDone();
    }

    /**
     * Returns a shallow collection copy for display or saving.
     * Adding or removing elements in the copy cannot change this list; task objects remain shared.
     */
    public ArrayList<Task> toList() {
        return new ArrayList<>(tasks);
    }
}
