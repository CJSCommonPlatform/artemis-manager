package uk.gov.justice.framework.tools.command;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.artemis.manager.connector.ArtemisConnector;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DeduplicateTopicMessagesTest {

    private PrintStream originalerr;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Mock
    private ArtemisConnector artemisConnector;

    @InjectMocks
    private DeduplicateTopicMessages deduplicateTopicMessages;

    @Before
    public void setUpStreams() {
        originalerr = System.err;
        System.setErr(new PrintStream(outContent));
    }

    @After
    public void cleanUpStreams() {
        System.setOut(originalerr);
    }

    @Test
    public void shouldInvokeConnector() throws Exception {
        deduplicateTopicMessages.jmxURLs = singletonList("service:jmx:rmi:///jndi/rmi://localhost:3000/jmxrmi");
        deduplicateTopicMessages.jmsURL = "tcp://localhost:61616";
        deduplicateTopicMessages.brokerName = "brokerabc";

        deduplicateTopicMessages.run(null);
        verify(artemisConnector).deduplicateTopicMessages("DLQ");
    }

    @Test
    public void shouldOutputException() {
        deduplicateTopicMessages.jmxURLs = singletonList("service:jmx:rmi:///jndi/rmi://localhost:3000/jmxrmi");
        deduplicateTopicMessages.jmsURL = "tcp://localhost:61616";
        deduplicateTopicMessages.brokerName = "brokerabc";

        when(artemisConnector.deduplicateTopicMessages("DLQ")).thenThrow(new RuntimeException("Test exception"));

        deduplicateTopicMessages.run(null);

        assertThat(outContent.toString(), containsString("java.lang.RuntimeException: Test exception"));
    }
}