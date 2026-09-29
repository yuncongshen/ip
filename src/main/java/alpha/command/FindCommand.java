package alpha.command;

import alpha.Storage;
import alpha.TaskList;
import alpha.Ui;

/**
 * Searches task descriptions without changing tasks or accessing storage.
 */
public class FindCommand extends Command {
    private final String keyword;

    /**
     * Creates a search for a validated, nonblank keyword or phrase.
     *
     * @param keyword The literal text to find in task descriptions.
     */
    public FindCommand(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showMatchingTasks(tasks.toList(), tasks.findDescriptionIndices(keyword));
    }
}
