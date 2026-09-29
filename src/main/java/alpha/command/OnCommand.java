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

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showDeadlinesOn(date, tasks.toList(), tasks.findDeadlineIndices(date));
    }
}
