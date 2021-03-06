package uk.gov.justice.framework.tools.command;

import static java.util.Collections.singletonList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.artemis.manager.connector.ArtemisConnector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class ReprocessTest {

    private static final byte[] NOT_USED_BYTES = "i123".getBytes();

    private PrintStream originalOut;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Mock
    private ArtemisConnector artemisConnector;

    @Captor
    private ArgumentCaptor<Iterator<String>> msgIdsIteratorCaptor;

    @InjectMocks
    private Reprocess reprocessCommand;

    @Before
    public void setUpStreams() {
        originalOut = System.out;
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void cleanUpStreams() {
        System.setOut(originalOut);
    }

    @Test
    public void shouldInvokeConnectorWithSingleMessageId() throws Exception {
        reprocessCommand.jmxURLs = singletonList("service:jmx:rmi:///jndi/rmi://localhost:3000/jmxrmi");
        reprocessCommand.brokerName = "brokerabc";
        reprocessCommand.msgId = "123456";

        reprocessCommand.run(null);

        verify(artemisConnector).reprocess(eq("DLQ"), msgIdsIteratorCaptor.capture());
        assertThat(msgIdsIteratorCaptor.getValue().next(), is("123456"));
    }

    @Test
    public void shouldInvokeConnectorWhenReceivingMultipleMessageIdsOnInput() throws Exception {
        reprocessCommand.jmxURLs = singletonList("service:jmx:rmi:///jndi/rmi://localhost:3000/jmxrmi");
        reprocessCommand.brokerName = "brokerabc";

        final InputStream sysIn = System.in;
        final ByteArrayInputStream in = new ByteArrayInputStream("id1 id2 id3".getBytes());
        System.setIn(in);

        reprocessCommand.run(null);
        System.setIn(sysIn);

        verify(artemisConnector).reprocess(eq("DLQ"), msgIdsIteratorCaptor.capture());
        final Iterator<String> msgIdsIteratorCaptor = this.msgIdsIteratorCaptor.getValue();
        assertThat(msgIdsIteratorCaptor.next(), is("id1"));
        assertThat(msgIdsIteratorCaptor.next(), is("id2"));
        assertThat(msgIdsIteratorCaptor.next(), is("id3"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldOutputNumnerOfReprocessedMessages() throws Exception {

        when(artemisConnector.reprocess(anyString(), any(Iterator.class))).thenReturn(3L);

        final InputStream sysIn = System.in;
        final ByteArrayInputStream in = new ByteArrayInputStream(NOT_USED_BYTES);
        System.setIn(in);

        reprocessCommand.run(null);
        System.setIn(sysIn);

        assertThat(outContent.toString(), is("{\"Command\":\"Reprocess message\",\"Occurrences\":3}\n"));
    }
}