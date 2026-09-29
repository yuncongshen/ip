package alpha.command;

import alpha.Storage;
import alpha.TaskList;
import alpha.Ui;

import java.time.LocalDate;

/**
 * Lists deadlines on a calendar date without changing tasks or accessing storage.
 */
public class OnCommand extends Command {
    private final LocalDate date;

    /**
     * Creates a query for a validated date.
     *
     * @param date The calendar date to match.
     */
    public OnCommand(LocalDate date) {
        this.date = date;
    }

    /**
     * Displays deadlines on the requested date, including all times and completion states.
     *
     * @param tasks The active task list to search.
     * @param ui The interface used to display matching deadlines.
     * @param storage The storage, unused by this command.
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showDeadlinesOn(date, tasks.toList(), tasks.findDeadlineIndices(date));
    }
}
