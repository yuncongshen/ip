package alpha;

import java.util.Scanner;

/**
 * Runs the Alpha chatbot command-line application.
 */
public class Alpha {
    private static final String DIVIDER = "    ____________________________________________________________";
    private static final int MAX_TASKS = 100;

    /**
     * Greets the user, manages tasks, lists them on request, and exits on {@code bye}.
     * Supported commands are {@code todo}, {@code deadline}, {@code event},
     * {@code list}, {@code mark}, {@code unmark}, and {@code bye}.
     *
     * @param args Command-line arguments, which are not used.
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

            taskCount = processCommand(command, tasks, taskCount);
            System.out.println(DIVIDER);
        }
    }

    /**
     * Processes a non-exit command and returns the resulting task count.
     *
     * @param command The user's command.
     * @param tasks The array of tasks.
     * @param taskCount The number of tasks stored.
     * @return The number of tasks stored after processing the command.
     */
    private static int processCommand(String command, Task[] tasks, int taskCount) {
        if (command.equals("list")) {
            System.out.println("     Here are the tasks in your list:");
            for (int i = 0; i < taskCount; i++) {
                System.out.println("     " + (i + 1) + "." + tasks[i]);
            }
        } else if (command.startsWith("mark ")) {
            markTask(command, tasks, taskCount);
        } else if (command.startsWith("unmark ")) {
            unmarkTask(command, tasks, taskCount);
        } else if (command.startsWith("todo ")) {
            taskCount = addTodo(command, tasks, taskCount);
        } else if (command.startsWith("deadline ")) {
            taskCount = addDeadline(command, tasks, taskCount);
        } else if (command.startsWith("event ")) {
            taskCount = addEvent(command, tasks, taskCount);
        } else if (taskCount < MAX_TASKS) {
            tasks[taskCount] = new Todo(command);
            taskCount++;
            System.out.println("     Got it. I've added this task:");
            System.out.println("       " + tasks[taskCount - 1]);
            System.out.println("     Now you have " + taskCount + " tasks in the list.");
        } else {
            System.out.println("     I cannot store more than " + MAX_TASKS + " tasks.");
        }

        return taskCount;
    }

    /**
     * Marks the task at the one-based index in the given command as done.
     *
     * @param command The user's input, for example, "mark 2".
     * @param tasks The array of tasks.
     * @param taskCount The number of tasks stored.
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
     * Marks the task at the one-based index in the given command as not done.
     *
     * @param command The user's input, for example, "unmark 2".
     * @param tasks The array of tasks.
     * @param taskCount The number of tasks stored.
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
     * Parses the task number from the given command into a zero-based index.
     * Prints a message and returns {@code null} if the number is not a valid positive integer
     * or does not refer to a stored task.
     *
     * @param command The user's input, for example, "mark 2".
     * @param prefixLength The length of the command prefix, for example, "mark ".
     * @param taskCount The number of tasks stored.
     * @return The zero-based task index, or {@code null} if invalid.
     */
    private static Integer parseIndex(String command, int prefixLength, int taskCount) {
        int index;
        try {
            index = Integer.parseInt(command.substring(prefixLength).trim()) - 1;
        } catch (NumberFormatException exception) {
            System.out.println("     Please give me a task number, e.g. \"mark 2\".");
            return null;
        }

        if (index < 0 || index >= taskCount) {
            System.out.println("     There is no task with that number.");
            return null;
        }

        return index;
    }

    /**
     * Adds a ToDo task described in the given command to the task array.
     *
     * @param command The user's input, for example, "todo borrow book".
     * @param tasks The array of tasks.
     * @param taskCount The number of tasks stored.
     * @return The updated number of tasks stored.
     */
    private static int addTodo(String command, Task[] tasks, int taskCount) {
        if (taskCount >= MAX_TASKS) {
            System.out.println("     I cannot store more than " + MAX_TASKS + " tasks.");
            return taskCount;
        }

        String description = command.substring("todo ".length());
        tasks[taskCount] = new Todo(description);
        return printAdded(tasks, taskCount);
    }

    /**
     * Adds a Deadline task described in the given command to the task array.
     * The description and deadline are separated by the "/by" marker.
     *
     * @param command The user's input, for example, "deadline return book /by Sunday".
     * @param tasks The array of tasks.
     * @param taskCount The number of tasks stored.
     * @return The updated number of tasks stored.
     */
    private static int addDeadline(String command, Task[] tasks, int taskCount) {
        if (taskCount >= MAX_TASKS) {
            System.out.println("     I cannot store more than " + MAX_TASKS + " tasks.");
            return taskCount;
        }

        String body = command.substring("deadline ".length());
        String[] parts = body.split(" /by ", 2);
        String description = parts[0];
        String by = parts.length > 1 ? parts[1] : "";
        tasks[taskCount] = new Deadline(description, by);
        return printAdded(tasks, taskCount);
    }

    /**
     * Adds an Event task described in the given command to the task array.
     * The description and start/end datetimes are separated by the "/from" and "/to" markers.
     *
     * @param command The user's input, for example, "event meeting /from Mon 2pm /to 4pm".
     * @param tasks The array of tasks.
     * @param taskCount The number of tasks stored.
     * @return The updated number of tasks stored.
     */
    private static int addEvent(String command, Task[] tasks, int taskCount) {
        if (taskCount >= MAX_TASKS) {
            System.out.println("     I cannot store more than " + MAX_TASKS + " tasks.");
            return taskCount;
        }

        String body = command.substring("event ".length());
        String[] parts = body.split(" /from ", 2);
        String description = parts[0];
        String[] times = parts.length > 1 ? parts[1].split(" /to ", 2) : new String[] {"", ""};
        String from = times[0];
        String to = times.length > 1 ? times[1] : "";
        tasks[taskCount] = new Event(description, from, to);
        return printAdded(tasks, taskCount);
    }

    /**
     * Prints a confirmation message for the newly added task and returns the updated task count.
     *
     * @param tasks The array of tasks.
     * @param taskCount The number of tasks stored before the addition.
     * @return The updated number of tasks stored.
     */
    private static int printAdded(Task[] tasks, int taskCount) {
        taskCount++;
        System.out.println("     Got it. I've added this task:");
        System.out.println("       " + tasks[taskCount - 1]);
        System.out.println("     Now you have " + taskCount + " tasks in the list.");
        return taskCount;
    }
}
