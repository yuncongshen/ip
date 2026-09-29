package alpha.command;

import alpha.AlphaException;
import alpha.Storage;
import alpha.TaskList;
import alpha.Ui;

/**
 * Marks a task as not done, saving the change before confirmation.
 */
public class UnmarkCommand extends Command {
    private final int index;

    /**
     * Creates a command for a task index already validated by the parser.
     *
     * @param index The zero-based task index, valid when this command executes.
     */
    public UnmarkCommand(int index) {
        this.index = index;
    }

    /**
     * {@inheritDoc}
     * Restores the previous completion status if saving fails.
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws AlphaException {
        boolean wasDone = tasks.isDone(index);
        tasks.unmark(index);
        try {
            storage.save(tasks.toList());
        } catch (AlphaException exception) {
            if (wasDone) {
                tasks.mark(index);
            }
            throw exception;
        }
        ui.showUnmarked(tasks.get(index));
    }
}
