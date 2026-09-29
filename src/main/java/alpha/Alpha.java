package alpha;

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

        ArrayList<Task> tasks = new ArrayList<>();
        try {
            storage.load(tasks);
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
    private void processCommand(Parser.CommandType commandType, String command, ArrayList<Task> tasks)
            throws AlphaException {
        switch (commandType) {
        case LIST -> ui.showTasks(tasks);
        case MARK -> markTask(parser.parseIndex(command, tasks.size()), tasks);
        case UNMARK -> unmarkTask(parser.parseIndex(command, tasks.size()), tasks);
        case DELETE -> deleteTask(parser.parseIndex(command, tasks.size()), tasks);
        case TODO, DEADLINE, EVENT -> {
            tasks.add(parser.parseTask(command));
            saveAddition(tasks);
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
    private void markTask(int index, ArrayList<Task> tasks)
            throws AlphaException {
        boolean wasDone = tasks.get(index).getStatusIcon().equals("X");
        tasks.get(index).markAsDone();
        try {
            storage.save(tasks);
        } catch (AlphaException exception) {
            if (!wasDone) {
                tasks.get(index).markAsNotDone();
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
    private void unmarkTask(int index, ArrayList<Task> tasks)
            throws AlphaException {
        boolean wasDone = tasks.get(index).getStatusIcon().equals("X");
        tasks.get(index).markAsNotDone();
        try {
            storage.save(tasks);
        } catch (AlphaException exception) {
            if (wasDone) {
                tasks.get(index).markAsDone();
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
    private void deleteTask(int index, ArrayList<Task> tasks)
            throws AlphaException {
        Task deletedTask = tasks.remove(index);
        try {
            storage.save(tasks);
        } catch (AlphaException exception) {
            tasks.add(index, deletedTask);
            throw exception;
        }

        ui.showDeleted(deletedTask, tasks.size());
    }

    /**
     * Saves an addition before confirming it, removing the new task if saving fails.
     *
     * @param tasks The list of tasks.
     * @throws AlphaException If the addition cannot be saved.
     */
    private void saveAddition(ArrayList<Task> tasks) throws AlphaException {
        try {
            storage.save(tasks);
        } catch (AlphaException exception) {
            tasks.remove(tasks.size() - 1);
            throw exception;
        }
        ui.showAdded(tasks.get(tasks.size() - 1), tasks.size());
    }
}
