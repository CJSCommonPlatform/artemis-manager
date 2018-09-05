package uk.gov.justice.artemis.manager.connector.jmx;

public class JmxProcessorFailureException extends RuntimeException {

    public JmxProcessorFailureException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
