package uk.gov.justice.artemis.manager.connector.filehandling;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TextMessageFileContentsReaderTest {

    @InjectMocks
    private TextMessageFileContentsReader textMessageFileContentsReader;

    @Test
    public void shouldReadContentsOfAMessageFileAsString() throws Exception {

        final String pathToFile = "src/test/resources/messages/messageForSendingToArtemis.txt";

        final String message = textMessageFileContentsReader.readContentsOf(pathToFile);

        assertThat(message, is("{\n    \"message\": \"All your base are belong to us\"\n}\n"));
    }

    @Test
    public void shouldFailIfFileNotFound() throws Exception {

        final String pathToFile = "this/file/does/not/exist.txt";

        try {
            textMessageFileContentsReader.readContentsOf(pathToFile);
            fail();
        } catch (final MessageFileException expected) {
            assertThat(expected.getMessage(), is("Failed to read file 'this/file/does/not/exist.txt'"));
        }
    }
}
