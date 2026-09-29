package alpha.command;

import alpha.AlphaException;
import alpha.Storage;
import alpha.TaskList;
import alpha.Ui;

/**
 * Represents an operation whose execution is separate from parsing user input.
 */
public abstract class Command {
    /**
     * Executes the operation using the application's tasks, interface, and storage.
     *
     * @param tasks The active task list.
     * @param ui The interface used to display the result.
     * @param storage The storage used to persist task changes.
     * @throws AlphaException If the operation cannot be completed.
     */
    public abstract void execute(TaskList tasks, Ui ui, Storage storage) throws AlphaException;

    /**
     * Returns whether this command requests application exit; ordinary commands do not.
     */
    public boolean isExit() {
        return false;
    }
}
