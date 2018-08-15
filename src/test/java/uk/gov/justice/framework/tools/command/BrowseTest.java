package uk.gov.justice.framework.tools.command;

import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import uk.gov.justice.artemis.manager.connector.ArtemisConnector;


@RunWith(MockitoJUnitRunner.class)
public class BrowseTest {

    @Mock
    ArtemisConnector artemisConnector;

    @InjectMocks
    Browse browseCommand;

    @Test
    public void shouldInvokeConnector() throws Exception {
        browseCommand.brokerName = "brokerabc";
        browseCommand.host = "some.host";
        browseCommand.port = "1212";

        browseCommand.run(null);
        verify(artemisConnector).messagesOf("some.host", "1212", "brokerabc", "DLQ");

    }
}