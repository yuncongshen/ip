import java.util.Scanner;

/**
 * Runs the Alpha chatbot command-line application.
 */
public class Alpha {
    private static final String DIVIDER = "    ____________________________________________________________";
    private static final int MAX_TASKS = 100;

    /**
     * Greets the user, stores/updates tasks, lists them on request, and exits on {@code bye}.
     * Supported commands: {@code list}, {@code mark}, {@code unmark}, {@code bye}.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        String banner = " █████╗ ██╗     ██████╗ ██╗  ██╗ █████╗ \n"
                + "██╔══██╗██║     ██╔══██╗██║  ██║██╔══██╗\n"
                + "███████║██║     ██████╔╝███████║███████║\n"
                + "██╔══██║██║     ██╔═══╝ ██╔══██║██╔══██║\n"
                + "██║  ██║███████╗██║     ██║  ██║██║  ██║\n"
                + "╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝\n";
        System.out.print(banner);
        System.out.println("Yooo! I'm Alpha. What can I help you with today?");
        System.out.println(DIVIDER);

        Task[] tasks = new Task[MAX_TASKS];
        int taskCount = 0;
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();

            if (command.equals("bye")) {
                System.out.println("     Bye. Hope to see you again soon!");
                System.out.println(DIVIDER);
                break;
            }

            if (command.equals("list")) {
                System.out.println("     Here are the tasks in your list:");
                for (int i = 0; i < taskCount; i++) {
                    System.out.println("     " + (i + 1) + "." + tasks[i]);
                }
            } else if (command.startsWith("mark ")) {
                markTask(command, tasks, taskCount);
            } else if (command.startsWith("unmark ")) {
                unmarkTask(command, tasks, taskCount);
            } else if (taskCount < MAX_TASKS) {
                tasks[taskCount] = new Task(command);
                taskCount++;
                System.out.println("     added: " + command);
            } else {
                System.out.println("     I cannot store more than " + MAX_TASKS + " tasks.");
            }
            System.out.println(DIVIDER);
        }
    }

    /**
     * Marks the task at the 1-based index in the given command as done.
     *
     * @param command   the user's input, e.g. "mark 2"
     * @param tasks     the array of tasks
     * @param taskCount the number of tasks stored
     */
    private static void markTask(String command, Task[] tasks, int taskCount) {
        Integer index = parseIndex(command, 5, taskCount);
        if (index == null) {
            return;
        }
        tasks[index].markAsDone();
        System.out.println("     Nice! I've marked this task as done:");
        System.out.println("       " + tasks[index]);
    }

    /**
     * Marks the task at the 1-based index in the given command as not done.
     *
     * @param command   the user's input, e.g. "unmark 2"
     * @param tasks     the array of tasks
     * @param taskCount the number of tasks stored
     */
    private static void unmarkTask(String command, Task[] tasks, int taskCount) {
        Integer index = parseIndex(command, 7, taskCount);
        if (index == null) {
            return;
        }
        tasks[index].markAsNotDone();
        System.out.println("     OK, I've marked this task as not done yet:");
        System.out.println("       " + tasks[index]);
    }

    /**
     * Parses the task number from the given command into a 0-based index.
     * Prints a message and returns {@code null} if the number is not a valid
     * positive integer or does not refer to a stored task.
     *
     * @param command   the user's input, e.g. "mark 2"
     * @param prefixLen the length of the command prefix (e.g. "mark ")
     * @param taskCount the number of tasks stored
     * @return the 0-based task index, or {@code null} if invalid
     */
    private static Integer parseIndex(String command, int prefixLen, int taskCount) {
        int index;
        try {
            index = Integer.parseInt(command.substring(prefixLen).trim()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("     Please give me a task number, e.g. \"mark 2\".");
            return null;
        }
        if (index < 0 || index >= taskCount) {
            System.out.println("     There is no task with that number.");
            return null;
        }
        return index;
    }
}
