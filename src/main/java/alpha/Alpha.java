package alpha;

import alpha.task.Deadline;
import alpha.task.Event;
import alpha.task.Task;
import alpha.task.Todo;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Runs the Alpha chatbot command-line application.
 */
public class Alpha {
    private static final String DIVIDER = "    ____________________________________________________________";

    /**
     * Greets the user, manages tasks, lists them on request, and exits on {@code bye}.
     * Supported commands are {@code todo}, {@code deadline}, {@code event},
     * {@code list}, {@code mark}, {@code unmark}, {@code delete}, and {@code bye}.
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

        ArrayList<Task> tasks = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();

            if (command.equals("bye")) {
                System.out.println("     Bye. Hope to see you again soon!");
                System.out.println(DIVIDER);
                break;
            }

            try {
                processCommand(command, tasks);
            } catch (AlphaException exception) {
                printError(exception.getMessage());
            }
            System.out.println(DIVIDER);
        }
    }

    /**
     * Processes a non-exit command and updates the task list.
     *
     * @param command The user's command.
     * @param tasks The list of tasks.
     * @throws AlphaException If the command is unrecognized.
     */
    private static void processCommand(String command, ArrayList<Task> tasks)
            throws AlphaException {
        if (command.equals("list")) {
            System.out.println("     Here are the tasks in your list:");
            for (int i = 0; i < tasks.size(); i++) {
                System.out.println("     " + (i + 1) + "." + tasks.get(i));
            }
        } else if (command.startsWith("mark ")) {
            markTask(command, tasks);
        } else if (command.startsWith("unmark ")) {
            unmarkTask(command, tasks);
        } else if (command.equals("delete") || command.startsWith("delete ")) {
            deleteTask(command, tasks);
        } else if (command.equals("todo") || command.startsWith("todo ")) {
            addTodo(command, tasks);
        } else if (command.equals("deadline") || command.startsWith("deadline ")) {
            addDeadline(command, tasks);
        } else if (command.equals("event") || command.startsWith("event ")) {
            addEvent(command, tasks);
        } else {
            throw new AlphaException("Bro, I don't know what that means...");
        }
    }

    /**
     * Marks the task at the one-based index in the given command as done.
     *
     * @param command The user's input, for example, "mark 2".
     * @param tasks The list of tasks.
     * @throws AlphaException If the task number is invalid.
     */
    private static void markTask(String command, ArrayList<Task> tasks)
            throws AlphaException {
        int index = parseIndex(command, 5, tasks.size());

        tasks.get(index).markAsDone();
        System.out.println("     Nice! I've marked this task as done:");
        System.out.println("       " + tasks.get(index));
    }

    /**
     * Marks the task at the one-based index in the given command as not done.
     *
     * @param command The user's input, for example, "unmark 2".
     * @param tasks The list of tasks.
     * @throws AlphaException If the task number is invalid.
     */
    private static void unmarkTask(String command, ArrayList<Task> tasks)
            throws AlphaException {
        int index = parseIndex(command, 7, tasks.size());

        tasks.get(index).markAsNotDone();
        System.out.println("     OK, I've marked this task as not done yet:");
        System.out.println("       " + tasks.get(index));
    }

    /**
     * Deletes a task while preserving the order of the remaining tasks.
     *
     * @param command The user's input, for example, "delete 3".
     * @param tasks The list of tasks.
     * @throws AlphaException If the task number is missing or invalid.
     */
    private static void deleteTask(String command, ArrayList<Task> tasks)
            throws AlphaException {
        int index = parseIndex(command, "delete".length(), tasks.size());
        Task deletedTask = tasks.remove(index);

        System.out.println("     Noted. I've removed this task:");
        System.out.println("       " + deletedTask);
        System.out.println("     Now you have " + tasks.size() + " tasks in the list.");
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
     * Adds a ToDo task described in the given command to the task list.
     *
     * @param command The user's input, for example, "todo borrow book".
     * @param tasks The list of tasks.
     * @throws AlphaException If the description is empty.
     */
    private static void addTodo(String command, ArrayList<Task> tasks)
            throws AlphaException {
        String description = command.length() > "todo ".length()
                ? command.substring("todo ".length()).trim()
                : "";
        if (description.isEmpty()) {
            throw new AlphaException("Bro, please add a description...");
        }
        tasks.add(new Todo(description));
        printAdded(tasks);
    }

    /**
     * Adds a Deadline task described in the given command to the task list.
     * The description and deadline are separated by the "/by" marker.
     *
     * @param command The user's input, for example, "deadline return book /by Sunday".
     * @param tasks The list of tasks.
     * @throws AlphaException If the description is empty.
     */
    private static void addDeadline(String command, ArrayList<Task> tasks)
            throws AlphaException {
        String body = command.length() > "deadline ".length()
                ? command.substring("deadline ".length()).trim()
                : "";
        String[] parts = body.split(" /by ", 2);
        String description = parts[0];
        if (description.isEmpty()) {
            throw new AlphaException("Bro, please add a description...");
        }
        String by = parts.length > 1 ? parts[1] : "";
        tasks.add(new Deadline(description, by));
        printAdded(tasks);
    }

    /**
     * Adds an Event task described in the given command to the task list.
     * The description and start/end datetimes are separated by the "/from" and "/to" markers.
     *
     * @param command The user's input, for example, "event meeting /from Mon 2pm /to 4pm".
     * @param tasks The list of tasks.
     * @throws AlphaException If the description is empty.
     */
    private static void addEvent(String command, ArrayList<Task> tasks)
            throws AlphaException {
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
        tasks.add(new Event(description, from, to));
        printAdded(tasks);
    }

    /**
     * Prints a confirmation message for the newly added task.
     *
     * @param tasks The list of tasks.
     */
    private static void printAdded(ArrayList<Task> tasks) {
        System.out.println("     Got it. I've added this task:");
        System.out.println("       " + tasks.get(tasks.size() - 1));
        System.out.println("     Now you have " + tasks.size() + " tasks in the list.");
    }
}
