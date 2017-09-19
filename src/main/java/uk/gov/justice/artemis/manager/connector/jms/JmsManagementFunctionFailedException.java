package uk.gov.justice.artemis.manager.connector.jms;

public class JmsManagementFunctionFailedException extends RuntimeException {

    public JmsManagementFunctionFailedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
