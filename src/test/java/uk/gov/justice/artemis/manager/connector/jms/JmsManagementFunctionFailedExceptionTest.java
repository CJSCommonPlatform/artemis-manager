package uk.gov.justice.artemis.manager.connector.jms;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;

public class JmsManagementFunctionFailedExceptionTest {

    @Test
    public void shouldConstructExceptionWithMessageAndCause() throws Exception {
        final String message = "message";
        final Exception cause = mock(Exception.class);

        final JmsManagementFunctionFailedException exception = new JmsManagementFunctionFailedException(message, cause);

        assertThat(exception.getMessage(), equalTo(message));
        assertThat(exception.getCause(), equalTo(cause));
    }
}