package alpha;

import alpha.task.Task;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Reads console commands and displays responses without changing tasks or saving data.
 */
public class Ui {
    private static final String MESSAGE_INDENT = "     ";
    private static final String TASK_INDENT = MESSAGE_INDENT + "  ";
    private static final String DIVIDER_INDENT = "    ";
    private static final String DIVIDER = "____________________________________________________________";
    private final Scanner scanner = new Scanner(System.in);

    /**
     * Returns whether another command is available, waiting for input if necessary.
     */
    public boolean hasNextCommand() {
        return scanner.hasNextLine();
    }

    /**
     * Reads the next command unchanged after {@link #hasNextCommand()} returns true.
     */
    public String readCommand() {
        return scanner.nextLine();
    }

    /**
     * Displays the startup banner, greeting, and opening divider.
     */
    public void showWelcome() {
        String banner = " █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ \n"
                + "██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗\n"
                + "███████║██║     ██████╔╝███████║███████║\n"
                + "██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║\n"
                + "██║  ██║███████╗██║     ██║  ██║██║  ██║\n"
                + "╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝\n";
        printMessage(banner);
        printMessage("Yooo! I'm Alpha. What can I help you with today?");
        showDivider();
    }

    /**
     * Displays a separator after a complete response.
     */
    public void showDivider() {
        printMessage(DIVIDER, DIVIDER_INDENT);
    }

    /**
     * Displays the farewell for an explicit exit command.
     */
    public void showGoodbye() {
        printMessage("Bye. Hope to see you again soon!");
    }

    /**
     * Displays an error message indented as chatbot output.
     *
     * @param message The error message to display.
     */
    public void showError(String message) {
        printMessage(message);
    }

    /**
     * Displays the tasks in order with one-based task numbers.
     *
     * @param tasks The tasks to display.
     */
    public void showTasks(List<Task> tasks) {
        printMessage("Here are the tasks in your list:");
        for (int i = 0; i < tasks.size(); i++) {
            printMessage((i + 1) + "." + tasks.get(i));
        }
    }

    /**
     * Displays matching deadlines with their original task numbers, or an explicit empty result.
     *
     * @param date The requested date.
     * @param tasks The full list of tasks.
     * @param indices The matching zero-based indices in list order.
     */
    public void showDeadlinesOn(LocalDate date, List<Task> tasks, List<Integer> indices) {
        String displayDate = date.format(DateTimeFormatter.ofPattern("MMM dd uuuu", Locale.ENGLISH));
        if (indices.isEmpty()) {
            printMessage("No deadlines on " + displayDate + ".");
            return;
        }
        printMessage("Here are your deadlines on " + displayDate + ":");
        for (int index : indices) {
            printMessage((index + 1) + "." + tasks.get(index));
        }
    }

    /**
     * Displays confirmation of a successfully saved addition.
     *
     * @param task The added task.
     * @param taskCount The number of tasks after adding.
     */
    public void showAdded(Task task, int taskCount) {
        printMessage("Got it. I've added this task:");
        printMessage(task.toString(), TASK_INDENT);
        showTaskCount(taskCount);
    }

    /**
     * Displays confirmation of a successfully saved deletion.
     *
     * @param task The deleted task.
     * @param taskCount The number of tasks remaining.
     */
    public void showDeleted(Task task, int taskCount) {
        printMessage("Noted. I've removed this task:");
        printMessage(task.toString(), TASK_INDENT);
        showTaskCount(taskCount);
    }

    /**
     * Displays confirmation that a task was successfully saved as done.
     *
     * @param task The updated task.
     */
    public void showMarked(Task task) {
        printMessage("Nice! I've marked this task as done:");
        printMessage(task.toString(), TASK_INDENT);
    }

    /**
     * Displays confirmation that a task was successfully saved as not done.
     *
     * @param task The updated task.
     */
    public void showUnmarked(Task task) {
        printMessage("OK, I've marked this task as not done yet:");
        printMessage(task.toString(), TASK_INDENT);
    }

    /**
     * Displays description-search results with their original task numbers.
     *
     * @param tasks The full task list.
     * @param indices Matching zero-based indices in list order.
     */
    public void showMatchingTasks(List<Task> tasks, List<Integer> indices) {
        printMessage("Here are the matching tasks in your list:");
        if (indices.isEmpty()) {
            printMessage("No matching tasks found.");
            return;
        }
        for (int index : indices) {
            printMessage((index + 1) + "." + tasks.get(index));
        }
    }

    /**
     * Displays the number of tasks remaining after a successful addition or deletion.
     *
     * @param taskCount The updated number of tasks.
     */
    private void showTaskCount(int taskCount) {
        printMessage("Now you have " + taskCount + " tasks in the list.");
    }

    /**
     * Prints every line of a response with the standard message indentation.
     */
    private void printMessage(String message) {
        printMessage(message, MESSAGE_INDENT);
    }

    /**
     * Applies the chosen indentation to each line, including multiline banners.
     */
    private void printMessage(String message, String indentation) {
        message.lines().forEach(line -> System.out.println(indentation + line));
    }
}
