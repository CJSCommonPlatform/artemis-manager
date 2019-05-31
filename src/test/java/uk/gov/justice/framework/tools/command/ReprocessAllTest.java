package uk.gov.justice.framework.tools.command;

import static java.util.Collections.singletonList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.artemis.manager.connector.ArtemisConnector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class ReprocessAllTest {

    private static final byte[] NOT_USED_BYTES = "i123".getBytes();

    private PrintStream originalOut;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Mock
    private ArtemisConnector artemisConnector;

    @InjectMocks
    private ReprocessAll reprocessAllCommand;

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
    public void shouldInvokeConnector() throws Exception {
        reprocessAllCommand.jmxURLs = singletonList("service:jmx:rmi:///jndi/rmi://localhost:3000/jmxrmi");
        reprocessAllCommand.brokerName = "brokerabc";
        reprocessAllCommand.msgId = "123456";

        reprocessAllCommand.run(null);

        verify(artemisConnector).reprocessAll(eq("DLQ"));
    }

    @Test
    public void shouldOutputNumberOfReprocessedMessages() throws Exception {

        when(artemisConnector.reprocessAll(anyString())).thenReturn(3);

        final InputStream sysIn = System.in;
        final ByteArrayInputStream in = new ByteArrayInputStream(NOT_USED_BYTES);
        System.setIn(in);

        reprocessAllCommand.run(null);
        System.setIn(sysIn);

        assertThat(outContent.toString(), is("{\"Command\":\"Reprocess all messages\",\"Occurrences\":3}\n"));
    }


    @Test
    public void shouldOutputZeroWhenArtemisException() throws Exception {

        when(artemisConnector.reprocessAll(anyString())).thenThrow(new RuntimeException("WENT WRONG"));

        final InputStream sysIn = System.in;
        final ByteArrayInputStream in = new ByteArrayInputStream(NOT_USED_BYTES);
        System.setIn(in);

        reprocessAllCommand.run(null);
        System.setIn(sysIn);

        assertThat(outContent.toString(), is("{\"Command\":\"Reprocess all messages\",\"Occurrences\":0}\n"));
    }
}