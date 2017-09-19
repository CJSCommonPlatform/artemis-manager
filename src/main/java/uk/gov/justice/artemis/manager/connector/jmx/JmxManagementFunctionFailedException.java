package uk.gov.justice.artemis.manager.connector.jmx;

public class JmxManagementFunctionFailedException extends RuntimeException {

    public JmxManagementFunctionFailedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
