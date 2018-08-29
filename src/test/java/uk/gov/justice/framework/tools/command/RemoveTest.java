package uk.gov.justice.framework.tools.command;

import static org.hamcrest.core.IsEqual.equalTo;
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
import java.util.Arrays;
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
public class RemoveTest {
    public static final byte[] NOT_USED_BYTES = "i123".getBytes();
    private PrintStream originalOut;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();


    @Mock
    ArtemisConnector artemisConnector;

    @Captor
    ArgumentCaptor<Iterator<String>> msgIdsIteratorCaptor;

    @InjectMocks
    Remove removeCommand;

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
        removeCommand.jmxURLs = Arrays.asList("service:jmx:rmi:///jndi/rmi://localhost:3000/jmxrmi");
        removeCommand.brokerName = "brokerabc";
        removeCommand.msgId = "123456";

        removeCommand.run(null);

        verify(artemisConnector).remove(eq("DLQ"), msgIdsIteratorCaptor.capture());
        assertThat(msgIdsIteratorCaptor.getValue().next(), equalTo("123456"));
    }

    @Test
    public void shouldInvokeConnectorWhenReceivingMultipleMessageIdsOnInput() throws Exception {
        removeCommand.jmxURLs = Arrays.asList("service:jmx:rmi:///jndi/rmi://localhost:3000/jmxrmi");
        removeCommand.brokerName = "brokerabc";

        final InputStream sysIn = System.in;
        final ByteArrayInputStream in = new ByteArrayInputStream("id1 id2 id3".getBytes());
        System.setIn(in);

        removeCommand.run(null);
        System.setIn(sysIn);
        verify(artemisConnector).remove(eq("DLQ"), msgIdsIteratorCaptor.capture());
        final Iterator<String> msgIdsIteratorCaptor = this.msgIdsIteratorCaptor.getValue();
        assertThat(msgIdsIteratorCaptor.next(), equalTo("id1"));
        assertThat(msgIdsIteratorCaptor.next(), equalTo("id2"));
        assertThat(msgIdsIteratorCaptor.next(), equalTo("id3"));
    }

    @Test
    public void shouldOutputNumnerOfRemovedMessages() throws Exception {
        when(artemisConnector.remove(anyString(), any(Iterator.class))).thenReturn(3l);

        final InputStream sysIn = System.in;
        final ByteArrayInputStream in = new ByteArrayInputStream(NOT_USED_BYTES);
        System.setIn(in);

        removeCommand.run(null);
        System.setIn(sysIn);

        assertThat(outContent.toString(), equalTo("{\"Command\":\"Remove message\",\"Occurrences\":3}\n"));
    }
}