package uk.gov.justice.artemis.manager.connector.jmx;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;

public class JmxManagementFunctionFailedExceptionTest {

    @Test
    public void shouldConstructExceptionWithMessageAndCause() throws Exception {
        final String message = "message";
        final Exception cause = mock(Exception.class);

        final JmxManagementFunctionFailedException exception = new JmxManagementFunctionFailedException(message, cause);

        assertThat(exception.getMessage(), is(message));
        assertThat(exception.getCause(), is(cause));
    }
}
