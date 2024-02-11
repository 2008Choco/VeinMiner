package wtf.choco.veinminer.update;

/**
 * An exception thrown when an unexpected HTTP status code was returned when performing
 * an update check and no other exception is applicable.
 */
public class UpdateFailException extends RuntimeException {

    private static final long serialVersionUID = 1021908815845998294L;

    private final int statusCode;
    private final String message;

    /**
     * Construct a new {@link UpdateFailException}.
     *
     * @param statusCode the status code that was returned
     * @param message the message to send
     */
    public UpdateFailException(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message + " (status code " + statusCode + ")";
    }

    /**
     * Get the status code that was returned.
     *
     * @return the status code
     */
    public int getStatusCode() {
        return statusCode;
    }

}
