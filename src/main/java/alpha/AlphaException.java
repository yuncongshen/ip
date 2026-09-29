package alpha;

/**
 * Represents a user-facing error while parsing a command, executing it, or loading saved tasks.
 */
public class AlphaException extends Exception {

    /**
     * Constructs an AlphaException with the given message.
     *
     * @param message The error message for the user.
     */
    public AlphaException(String message) {
        super(message);
    }
}
