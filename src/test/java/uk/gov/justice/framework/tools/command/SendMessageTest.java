package uk.gov.justice.framework.tools.command;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.artemis.manager.connector.ArtemisConnector;
import uk.gov.justice.artemis.manager.connector.filehandling.MessageFileException;
import uk.gov.justice.artemis.manager.connector.filehandling.TextMessageFileContentsReader;
import uk.gov.justice.output.OutputPrinter;

import java.net.MalformedURLException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SendMessageTest {

    @Mock
    private OutputPrinter outputPrinter;

    @Mock
    private ArtemisConnector artemisConnector;

    private final TextMessageFileContentsReader textMessageFileContentsReader = mock(TextMessageFileContentsReader.class);

    @InjectMocks
    private SendMessage sendMessage = new SendMessage(textMessageFileContentsReader) {
        @Override
        public void setup() throws MalformedURLException {
        }
    };

    @Captor
    private ArgumentCaptor<Exception> exceptionCaptor;

    @Test
    public void shouldSendReadContentsOfSuppliedFileAndSendAsTextMessage() throws Exception {

        final String fileName = "some/messageFile.txt";
        final String message = "a message";
        final String results = "this message succeeded";

        sendMessage.textMessageFile = fileName;

        when(textMessageFileContentsReader.readContentsOf(fileName)).thenReturn(message);
        when(artemisConnector.sendTextMessage("DLQ", message)).thenReturn(results);

        sendMessage.run(new String[0]);

        verify(outputPrinter).write(results);
    }

    @Test
    public void shouldFailIfSuppliedFileNameIsNull() throws Exception {

        sendMessage.textMessageFile = null;

        sendMessage.run(new String[0]);

        verify(outputPrinter).writeStackTrace(exceptionCaptor.capture());

        final Exception exception = exceptionCaptor.getValue();

        assertThat(exception, is(instanceOf(MessageFileException.class)));
        assertThat(exception.getMessage(), is("Cannot read message file. No file name set. Please use -messageFile parameter when calling this command"));
    }

    @Test
    public void shouldFailIfSuppliedFileNameIsEmpty() throws Exception {

        sendMessage.textMessageFile = "";

        sendMessage.run(new String[0]);

        verify(outputPrinter).writeStackTrace(exceptionCaptor.capture());

        final Exception exception = exceptionCaptor.getValue();

        assertThat(exception, is(instanceOf(MessageFileException.class)));
        assertThat(exception.getMessage(), is("Cannot read message file. No file name set. Please use -messageFile parameter when calling this command"));
    }

    @Test
    public void shouldFailIfWriteExceptionIfSendingMessageFails() throws Exception {

        final NullPointerException nullPointerException = new NullPointerException("Ooops");

        final String fileName = "some/messageFile.txt";
        final String message = "a message";

        sendMessage.textMessageFile = fileName;

        when(textMessageFileContentsReader.readContentsOf(fileName)).thenReturn(message);
        when(artemisConnector.sendTextMessage("DLQ", message)).thenThrow(nullPointerException);

        sendMessage.run(new String[0]);

        verify(outputPrinter).writeStackTrace(exceptionCaptor.capture());

        final Exception exception = exceptionCaptor.getValue();

        assertThat(exception, is(nullPointerException));
    }
}
