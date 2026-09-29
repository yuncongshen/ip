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
    private static final String DIVIDER = "    ____________________________________________________________";
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
        System.out.print(banner);
        System.out.println("Yooo! I'm Alpha. What can I help you with today?");
        showDivider();
    }

    /**
     * Displays a separator after a complete response.
     */
    public void showDivider() {
        System.out.println(DIVIDER);
    }

    /**
     * Displays the farewell for an explicit exit command.
     */
    public void showGoodbye() {
        System.out.println("     Bye. Hope to see you again soon!");
    }

    /**
     * Displays an error message indented as chatbot output.
     *
     * @param message The error message to display.
     */
    public void showError(String message) {
        System.out.println("     " + message);
    }

    /**
     * Displays the tasks in order with one-based task numbers.
     *
     * @param tasks The tasks to display.
     */
    public void showTasks(List<Task> tasks) {
        System.out.println("     Here are the tasks in your list:");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println("     " + (i + 1) + "." + tasks.get(i));
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
            System.out.println("     No deadlines on " + displayDate + ".");
            return;
        }
        System.out.println("     Here are your deadlines on " + displayDate + ":");
        for (int index : indices) {
            System.out.println("     " + (index + 1) + "." + tasks.get(index));
        }
    }

    /**
     * Displays confirmation of a successfully saved addition.
     *
     * @param task The added task.
     * @param taskCount The number of tasks after adding.
     */
    public void showAdded(Task task, int taskCount) {
        System.out.println("     Got it. I've added this task:");
        System.out.println("       " + task);
        showTaskCount(taskCount);
    }

    /**
     * Displays confirmation of a successfully saved deletion.
     *
     * @param task The deleted task.
     * @param taskCount The number of tasks remaining.
     */
    public void showDeleted(Task task, int taskCount) {
        System.out.println("     Noted. I've removed this task:");
        System.out.println("       " + task);
        showTaskCount(taskCount);
    }

    /**
     * Displays confirmation that a task was successfully saved as done.
     *
     * @param task The updated task.
     */
    public void showMarked(Task task) {
        System.out.println("     Nice! I've marked this task as done:");
        System.out.println("       " + task);
    }

    /**
     * Displays confirmation that a task was successfully saved as not done.
     *
     * @param task The updated task.
     */
    public void showUnmarked(Task task) {
        System.out.println("     OK, I've marked this task as not done yet:");
        System.out.println("       " + task);
    }

    private void showTaskCount(int taskCount) {
        System.out.println("     Now you have " + taskCount + " tasks in the list.");
    }
}
