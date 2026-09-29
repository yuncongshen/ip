package alpha.command;

import alpha.Storage;
import alpha.TaskList;
import alpha.Ui;

/**
 * Displays the farewell and requests exit without changing tasks or writing to storage.
 */
public class ExitCommand extends Command {
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showGoodbye();
    }

    @Override
    public boolean isExit() {
        return true;
    }
}
