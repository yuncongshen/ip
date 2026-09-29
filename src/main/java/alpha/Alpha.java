package alpha;

import alpha.command.AddCommand;
import alpha.command.Command;
import alpha.task.Task;

import java.nio.file.Path;
import java.util.ArrayList;

/**
 * Runs the Alpha chatbot command-line application.
 */
public class Alpha {
    private final Parser parser = new Parser();
    private final Ui ui = new Ui();
    private final Storage storage = new Storage(Path.of("data", "alpha.txt"));

    /**
     * Greets the user, restores saved tasks, manages them, and exits on {@code bye}.
     * Supported commands are {@code todo}, {@code deadline}, {@code event},
     * {@code list}, {@code mark}, {@code unmark}, {@code delete}, and {@code bye}.
     *
     * @param args Command-line arguments, which are not used.
     */
    public static void main(String[] args) {
        new Alpha().run();
    }

    /**
     * Restores tasks and handles commands until exit, end of input, or a loading error.
     */
    public void run() {
        ui.showWelcome();

        TaskList tasks;
        try {
            ArrayList<Task> restoredTasks = new ArrayList<>();
            storage.load(restoredTasks);
            tasks = new TaskList(restoredTasks);
        } catch (AlphaException exception) {
            ui.showError(exception.getMessage());
            ui.showDivider();
            return;
        }
        while (ui.hasNextCommand()) {
            String command = ui.readCommand();

            try {
                Parser.CommandType commandType = parser.parseCommandType(command);
                if (commandType == Parser.CommandType.BYE) {
                    ui.showGoodbye();
                    ui.showDivider();
                    break;
                }
                processCommand(commandType, command, tasks);
            } catch (AlphaException exception) {
                ui.showError(exception.getMessage());
            }
            ui.showDivider();
        }
    }

    /**
     * Processes a non-exit command and updates the task list.
     *
     * @param commandType The recognized non-exit operation.
     * @param command The user's command.
     * @param tasks The list of tasks.
     * @throws AlphaException If the command is invalid or its change cannot be saved.
     */
    private void processCommand(Parser.CommandType commandType, String command, TaskList tasks)
            throws AlphaException {
        switch (commandType) {
        case LIST -> ui.showTasks(tasks.toList());
        case MARK -> markTask(parser.parseIndex(command, tasks.size()), tasks);
        case UNMARK -> unmarkTask(parser.parseIndex(command, tasks.size()), tasks);
        case DELETE -> deleteTask(parser.parseIndex(command, tasks.size()), tasks);
        case TODO, DEADLINE, EVENT -> {
            Command addition = new AddCommand(parser.parseTask(command));
            addition.execute(tasks, ui, storage);
        }
        default -> throw new IllegalArgumentException("Exit commands are handled by run.");
        }
    }

    /**
     * Marks the task at the validated zero-based index as done.
     *
     * @param index The validated zero-based task index.
     * @param tasks The list of tasks.
     * @throws AlphaException If the change cannot be saved.
     */
    private void markTask(int index, TaskList tasks)
            throws AlphaException {
        boolean wasDone = tasks.isDone(index);
        tasks.mark(index);
        try {
            storage.save(tasks.toList());
        } catch (AlphaException exception) {
            if (!wasDone) {
                tasks.unmark(index);
            }
            throw exception;
        }
        ui.showMarked(tasks.get(index));
    }

    /**
     * Marks the task at the validated zero-based index as not done.
     *
     * @param index The validated zero-based task index.
     * @param tasks The list of tasks.
     * @throws AlphaException If the change cannot be saved.
     */
    private void unmarkTask(int index, TaskList tasks)
            throws AlphaException {
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

    /**
     * Deletes a task while preserving the order of the remaining tasks.
     *
     * @param index The validated zero-based task index.
     * @param tasks The list of tasks.
     * @throws AlphaException If the deletion cannot be saved.
     */
    private void deleteTask(int index, TaskList tasks)
            throws AlphaException {
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
