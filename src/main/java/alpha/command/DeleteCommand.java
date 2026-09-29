package alpha.command;

import alpha.AlphaException;
import alpha.Storage;
import alpha.TaskList;
import alpha.Ui;
import alpha.task.Task;

/**
 * Deletes a task while preserving the order of remaining tasks, saving the change before confirmation.
 */
public class DeleteCommand extends Command {
    private final int index;

    /**
     * Creates a command for a task index already validated by the parser.
     *
     * @param index The zero-based task index, valid when this command executes.
     */
    public DeleteCommand(int index) {
        this.index = index;
    }

    /**
     * {@inheritDoc}
     * Restores the deleted task at its original position if saving fails.
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws AlphaException {
        Task deletedTask = tasks.delete(index);
        try {
            storage.save(tasks.toList());
        } catch (AlphaException exception) {
            tasks.insert(index, deletedTask);
            throw exception;
        }

        ui.showDeleted(deletedTask, tasks.size());
    }
}
