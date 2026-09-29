package alpha;

import alpha.command.AddCommand;
import alpha.command.Command;
import alpha.command.DeleteCommand;
import alpha.command.ExitCommand;
import alpha.command.ListCommand;
import alpha.command.MarkCommand;
import alpha.command.OnCommand;
import alpha.command.UnmarkCommand;
import alpha.task.Deadline;
import alpha.task.Event;
import alpha.task.Task;
import alpha.task.Todo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

/**
 * Interprets command text without changing the task list, displaying output, or accessing storage.
 */
public class Parser {
    private static final DateTimeFormatter QUERY_DATE = DateTimeFormatter.ofPattern("d/M/uuuu", Locale.ENGLISH)
            .withResolverStyle(ResolverStyle.STRICT);

    /**
     * Identifies the operations supported by the command-line interface.
     */
    private enum CommandType {
        LIST, MARK, UNMARK, DELETE, TODO, DEADLINE, EVENT, BYE, ON
    }

    /**
     * Recognizes a command using the existing case-sensitive spelling and spacing rules.
     *
     * @param command The unmodified user input.
     * @return The recognized operation.
     * @throws AlphaException If the command is unknown.
     */
    private CommandType parseCommandType(String command) throws AlphaException {
        if (command.equals("list")) {
            return CommandType.LIST;
        } else if (command.equals("bye")) {
            return CommandType.BYE;
        } else if (command.equals("mark") || command.startsWith("mark ")) {
            return CommandType.MARK;
        } else if (command.equals("unmark") || command.startsWith("unmark ")) {
            return CommandType.UNMARK;
        } else if (command.equals("delete") || command.startsWith("delete ")) {
            return CommandType.DELETE;
        } else if (command.equals("todo") || command.startsWith("todo ")) {
            return CommandType.TODO;
        } else if (command.equals("deadline") || command.startsWith("deadline ")) {
            return CommandType.DEADLINE;
        } else if (command.equals("event") || command.startsWith("event ")) {
            return CommandType.EVENT;
        } else if (command.equals("on") || command.startsWith("on ")) {
            return CommandType.ON;
        }
        throw new AlphaException("Bro, I don't know what that means...");
    }

    /**
     * Creates a command without executing it or changing application state.
     * Indexed commands are validated against the current list size and must execute before the list changes.
     *
     * @param command The unmodified user input.
     * @param taskCount The current number of tasks, used to validate task numbers.
     * @return The operation ready for immediate execution.
     * @throws AlphaException If the command or its arguments are invalid.
     */
    public Command parse(String command, int taskCount) throws AlphaException {
        return switch (parseCommandType(command)) {
        case LIST -> new ListCommand();
        case BYE -> new ExitCommand();
        case MARK -> new MarkCommand(parseIndex(command, taskCount));
        case UNMARK -> new UnmarkCommand(parseIndex(command, taskCount));
        case DELETE -> new DeleteCommand(parseIndex(command, taskCount));
        case TODO -> new AddCommand(parseTodo(command));
        case DEADLINE -> new AddCommand(parseDeadline(command));
        case EVENT -> new AddCommand(parseEvent(command));
        case ON -> new OnCommand(parseQueryDate(command));
        };
    }

    /**
     * Parses a calendar date for a query, rejecting times and impossible dates.
     */
    private LocalDate parseQueryDate(String command) throws AlphaException {
        String date = command.substring("on".length()).trim();
        try {
            if (date.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
                return LocalDate.parse(date);
            } else if (date.matches("[0-9]{1,2}/[0-9]{1,2}/[0-9]{4}")) {
                return LocalDate.parse(date, QUERY_DATE);
            }
        } catch (DateTimeParseException exception) {
            throw new AlphaException("Invalid query date; use on yyyy-MM-dd or on d/M/yyyy");
        }
        throw new AlphaException("Invalid query date; use on yyyy-MM-dd or on d/M/yyyy");
    }

    /**
     * Parses the task number from the given command into a zero-based index.
     *
     * @param command The user's input, for example, "mark 2".
     * @param taskCount The number of tasks stored.
     * @return The zero-based task index.
     * @throws AlphaException If the number is not a valid positive integer
     *                        or does not refer to a stored task.
     */
    private int parseIndex(String command, int taskCount)
            throws AlphaException {
        int separator = command.indexOf(' ');
        String argument = separator < 0 ? "" : command.substring(separator).trim();
        int taskNumber;
        try {
            taskNumber = Integer.parseInt(argument);
        } catch (NumberFormatException exception) {
            throw new AlphaException("Please give me a task number, e.g. \"mark 2\"...");
        }

        if (taskNumber <= 0 || taskNumber > taskCount) {
            throw new AlphaException("There is no task with that number...");
        }

        return taskNumber - 1;
    }

    /**
     * Creates a todo from the description in an addition command.
     *
     * @param command The user's input, for example, "todo borrow book".
     * @throws AlphaException If the description is empty.
     */
    private Task parseTodo(String command)
            throws AlphaException {
        String description = command.length() > "todo ".length()
                ? command.substring("todo ".length()).trim()
                : "";
        if (description.isEmpty()) {
            throw new AlphaException("Bro, please add a description...");
        }
        return new Todo(description);
    }

    /**
     * Creates a deadline from the description and required date with an optional time.
     * The description and deadline are separated by the "/by" marker.
     *
     * @param command The user's input, for example, "deadline return book /by 2019-10-15".
     * @throws AlphaException If the description is empty or the date is invalid.
     */
    private Task parseDeadline(String command)
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
        try {
            return new Deadline(description, by);
        } catch (IllegalArgumentException exception) {
            throw new AlphaException(exception.getMessage());
        }
    }

    /**
     * Creates an event from the description and optional time fields.
     * The description and start/end datetimes are separated by the "/from" and "/to" markers.
     *
     * @param command The user's input, for example, "event meeting /from Mon 2pm /to 4pm".
     * @throws AlphaException If the description is empty.
     */
    private Task parseEvent(String command)
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
        return new Event(description, from, to);
    }
}
