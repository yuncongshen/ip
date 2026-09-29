package alpha.command;

import alpha.Storage;
import alpha.TaskList;
import alpha.Ui;

/**
 * Displays the task list without changing tasks or writing to storage.
 */
public class ListCommand extends Command {
    /**
     * Displays all tasks in list order without modifying or saving them.
     *
     * @param tasks The active task list.
     * @param ui The interface used to display the list.
     * @param storage The storage, unused by this command.
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showTasks(tasks.toList());
    }
}
