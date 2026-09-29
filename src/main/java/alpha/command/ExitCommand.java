package alpha.command;

import alpha.Storage;
import alpha.TaskList;
import alpha.Ui;

/**
 * Displays the farewell and requests exit without changing tasks or writing to storage.
 */
public class ExitCommand extends Command {
    /**
     * Displays the farewell without changing tasks or saving data.
     *
     * @param tasks The active task list, unused by this command.
     * @param ui The interface used to display the farewell.
     * @param storage The storage, unused by this command.
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showGoodbye();
    }

    /**
     * Returns true to tell the application loop to stop after execution.
     */
    @Override
    public boolean isExit() {
        return true;
    }
}
