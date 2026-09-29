package alpha.command;

import alpha.AlphaException;
import alpha.Storage;
import alpha.TaskList;
import alpha.Ui;
import alpha.task.Task;

/**
 * Adds a parsed task and saves the change before displaying confirmation.
 * The same operation supports todos, deadlines, and events.
 */
public class AddCommand extends Command {
    private final Task task;

    /**
     * Creates an addition for a task already validated by the parser.
     *
     * @param task The task to add.
     */
    public AddCommand(Task task) {
        this.task = task;
    }

    /**
     * {@inheritDoc}
     * Removes the newly appended task if saving fails, then propagates the error.
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws AlphaException {
        tasks.add(task);
        try {
            storage.save(tasks.toList());
        } catch (AlphaException exception) {
            tasks.delete(tasks.size() - 1);
            throw exception;
        }
        ui.showAdded(task, tasks.size());
    }
}
