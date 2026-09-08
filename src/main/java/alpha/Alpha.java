package alpha;

import alpha.task.Deadline;
import alpha.task.Event;
import alpha.task.Task;
import alpha.task.Todo;

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

            try {
                taskCount = processCommand(command, tasks, taskCount);
            } catch (AlphaException exception) {
                printError(exception.getMessage());
            }
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
     * @throws AlphaException If the command is unrecognized.
     */
    private static int processCommand(String command, Task[] tasks, int taskCount)
            throws AlphaException {
        if (command.equals("list")) {
            System.out.println("     Here are the tasks in your list:");
            for (int i = 0; i < taskCount; i++) {
                System.out.println("     " + (i + 1) + "." + tasks[i]);
            }
        } else if (command.startsWith("mark ")) {
            markTask(command, tasks, taskCount);
        } else if (command.startsWith("unmark ")) {
            unmarkTask(command, tasks, taskCount);
        } else if (command.equals("todo") || command.startsWith("todo ")) {
            taskCount = addTodo(command, tasks, taskCount);
        } else if (command.equals("deadline") || command.startsWith("deadline ")) {
            taskCount = addDeadline(command, tasks, taskCount);
        } else if (command.equals("event") || command.startsWith("event ")) {
            taskCount = addEvent(command, tasks, taskCount);
        } else {
            throw new AlphaException("Bro, I don't know what that means...");
        }

        return taskCount;
    }

    /**
     * Marks the task at the one-based index in the given command as done.
     *
     * @param command The user's input, for example, "mark 2".
     * @param tasks The array of tasks.
     * @param taskCount The number of tasks stored.
     * @throws AlphaException If the task number is invalid.
     */
    private static void markTask(String command, Task[] tasks, int taskCount)
            throws AlphaException {
        int index = parseIndex(command, 5, taskCount);

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
     * @throws AlphaException If the task number is invalid.
     */
    private static void unmarkTask(String command, Task[] tasks, int taskCount)
            throws AlphaException {
        int index = parseIndex(command, 7, taskCount);

        tasks[index].markAsNotDone();
        System.out.println("     OK, I've marked this task as not done yet:");
        System.out.println("       " + tasks[index]);
    }

    /**
     * Parses the task number from the given command into a zero-based index.
     *
     * @param command The user's input, for example, "mark 2".
     * @param prefixLength The length of the command prefix, for example, "mark ".
     * @param taskCount The number of tasks stored.
     * @return The zero-based task index.
     * @throws AlphaException If the number is not a valid positive integer
     *                        or does not refer to a stored task.
     */
    private static int parseIndex(String command, int prefixLength, int taskCount)
            throws AlphaException {
        int index;
        try {
            index = Integer.parseInt(command.substring(prefixLength).trim()) - 1;
        } catch (NumberFormatException exception) {
            throw new AlphaException("Please give me a task number, e.g. \"mark 2\"...");
        }

        if (index < 0 || index >= taskCount) {
            throw new AlphaException("There is no task with that number...");
        }

        return index;
    }

    /**
     * Prints the given error message indented as chatbot output.
     *
     * @param message The error message to display.
     */
    private static void printError(String message) {
        System.out.println("     " + message);
    }

    /**
     * Adds a ToDo task described in the given command to the task array.
     *
     * @param command The user's input, for example, "todo borrow book".
     * @param tasks The array of tasks.
     * @param taskCount The number of tasks stored.
     * @return The updated number of tasks stored.
     * @throws AlphaException If the description is empty or the task array is full.
     */
    private static int addTodo(String command, Task[] tasks, int taskCount)
            throws AlphaException {
        if (taskCount >= MAX_TASKS) {
            throw new AlphaException("I cannot store more than " + MAX_TASKS + " tasks...");
        }

        String description = command.length() > "todo ".length()
                ? command.substring("todo ".length()).trim()
                : "";
        if (description.isEmpty()) {
            throw new AlphaException("Bro, please add a description...");
        }
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
     * @throws AlphaException If the description is empty or the task array is full.
     */
    private static int addDeadline(String command, Task[] tasks, int taskCount)
            throws AlphaException {
        if (taskCount >= MAX_TASKS) {
            throw new AlphaException("I cannot store more than " + MAX_TASKS + " tasks...");
        }

        String body = command.length() > "deadline ".length()
                ? command.substring("deadline ".length()).trim()
                : "";
        String[] parts = body.split(" /by ", 2);
        String description = parts[0];
        if (description.isEmpty()) {
            throw new AlphaException("Bro, please add a description...");
        }
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
     * @throws AlphaException If the description is empty or the task array is full.
     */
    private static int addEvent(String command, Task[] tasks, int taskCount)
            throws AlphaException {
        if (taskCount >= MAX_TASKS) {
            throw new AlphaException("I cannot store more than " + MAX_TASKS + " tasks...");
        }

        String body = command.length() > "event ".length()
                ? command.substring("event ".length()).trim()
                : "";
        String[] parts = body.split(" /from ", 2);
        String description = parts[0];
        if (description.isEmpty()) {
            throw new AlphaException("Bro, please add a description...");
        }
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
