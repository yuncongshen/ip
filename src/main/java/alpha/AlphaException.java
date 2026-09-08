package alpha;

/**
 * Represents an error that occurs while processing a user command.
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
