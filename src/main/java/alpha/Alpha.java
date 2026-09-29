package alpha;

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
     * {@code list}, {@code on}, {@code mark}, {@code unmark}, {@code delete}, and {@code bye}.
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
        boolean isExit = false;
        while (!isExit && ui.hasNextCommand()) {
            try {
                String fullCommand = ui.readCommand();
                Command command = parser.parse(fullCommand, tasks.size());
                command.execute(tasks, ui, storage);
                isExit = command.isExit();
            } catch (AlphaException exception) {
                ui.showError(exception.getMessage());
            } finally {
                ui.showDivider();
            }
        }
    }
}
