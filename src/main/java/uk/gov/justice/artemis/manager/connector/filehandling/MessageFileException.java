package uk.gov.justice.artemis.manager.connector.filehandling;

public class MessageFileException extends RuntimeException {

    public MessageFileException(final String message) {
        super(message);
    }

    public MessageFileException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
