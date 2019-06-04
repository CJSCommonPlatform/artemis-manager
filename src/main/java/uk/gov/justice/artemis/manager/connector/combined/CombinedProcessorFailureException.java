package uk.gov.justice.artemis.manager.connector.combined;

public class CombinedProcessorFailureException extends RuntimeException {


    public CombinedProcessorFailureException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
