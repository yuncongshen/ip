package alpha;

import alpha.command.AddCommand;
import alpha.command.Command;
import alpha.command.DeleteCommand;
import alpha.command.MarkCommand;
import alpha.command.UnmarkCommand;
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
        if (commandType == Parser.CommandType.LIST) {
            ui.showTasks(tasks.toList());
            return;
        }
        Command operation = switch (commandType) {
        case MARK -> new MarkCommand(parser.parseIndex(command, tasks.size()));
        case UNMARK -> new UnmarkCommand(parser.parseIndex(command, tasks.size()));
        case DELETE -> new DeleteCommand(parser.parseIndex(command, tasks.size()));
        case TODO, DEADLINE, EVENT -> new AddCommand(parser.parseTask(command));
        default -> throw new IllegalArgumentException("Exit commands are handled by run.");
        };
        operation.execute(tasks, ui, storage);
    }
}
