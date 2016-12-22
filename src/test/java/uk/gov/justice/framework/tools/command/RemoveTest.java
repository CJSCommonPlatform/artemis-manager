package uk.gov.justice.framework.tools.command;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.notNull;
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
public class RemoveTest {
    private PrintStream originalOut;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Mock
    ArtemisConnector artemisConnector;

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
    public void shouldInvokeConnector() throws Exception {
        removeCommand.brokerName = "brokerabc";
        removeCommand.host = "some.host";
        removeCommand.port = "1212";
        removeCommand.msgId = "123456";

        removeCommand.run(null);

        verify(artemisConnector).removeMessage("some.host", "1212", "brokerabc", "DLQ", "123456");

    }

    @Test
    public void shouldReturnIdOfRemovedMessage() throws Exception {
        removeCommand.brokerName = "brokerdef";
        removeCommand.host = "some.host.abc";
        removeCommand.port = "111";
        removeCommand.msgId = "22555666";

        when(artemisConnector.removeMessage("some.host.abc", "111", "brokerdef", "DLQ", "22555666")).thenReturn(true);
        removeCommand.run(null);
        assertThat(outContent.toString(), is("Removed message 22555666\n"));

    }

    @Test
    public void shouldReturnIdOfNOTRemovedMessage() throws Exception {
        removeCommand.brokerName = "brokerdef";
        removeCommand.host = "some.host.abc";
        removeCommand.port = "111";
        removeCommand.msgId = "22555666";

        when(artemisConnector.removeMessage("some.host.abc", "111", "brokerdef", "DLQ", "22555666")).thenReturn(false);
        removeCommand.run(null);
        assertThat(outContent.toString(), is("Could NOT remove message 22555666\n"));

    }

    @Test
    public void shouldReturnIdOfAMessageIfExceptionThrown() throws Exception {
        removeCommand.brokerName = "brok123";
        removeCommand.host = "hosta";
        removeCommand.port = "333";
        removeCommand.msgId = "4444444";

        when(artemisConnector.removeMessage("hosta", "333", "brok123", "DLQ", "4444444")).thenThrow(new IllegalArgumentException());
        removeCommand.run(null);
        assertThat(outContent.toString(), is("Could NOT remove message 4444444\n"));


    }


}