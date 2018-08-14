package uk.gov.justice.framework.tools.command;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import uk.gov.justice.artemis.manager.connector.ArtemisConnector;


@RunWith(MockitoJUnitRunner.class)
public class ListQueuesTest {

    @Mock
    ArtemisConnector artemisConnector;

    @InjectMocks
    ListQueues listQueuesCommand;

    @Test
    public void shouldInvokeConnector() throws Exception {
        listQueuesCommand.brokerName = "brokerabc";
        listQueuesCommand.host = "some.host";
        listQueuesCommand.port = "1212";
        when(artemisConnector.queueNames(anyString(), anyString(), anyString())).thenReturn(new String[] {"DLQ", "ExpiryQueue" });

        listQueuesCommand.run(null);
        verify(artemisConnector).queueNames("some.host", "1212", "brokerabc");
    }
}