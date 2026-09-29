package alpha.command;

import alpha.Storage;
import alpha.TaskList;
import alpha.Ui;

/**
 * Displays the task list without changing tasks or writing to storage.
 */
public class ListCommand extends Command {
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showTasks(tasks.toList());
    }
}
