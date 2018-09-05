package uk.gov.justice.artemis.manager.connector.jms;

public class JmsProcessorFailureException extends RuntimeException {

    public JmsProcessorFailureException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
