package uk.gov.justice.framework.tools.command;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.artemis.manager.connector.ArtemisConnector;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class ListQueuesTest {

    @Mock
    private ArtemisConnector artemisConnector;

    @InjectMocks
    private ListQueues listQueuesCommand;

    @Test
    public void shouldInvokeConnector() throws Exception {
        listQueuesCommand.jmxURLs = singletonList("service:jmx:rmi:///jndi/rmi://localhost:3000/jmxrmi");
        listQueuesCommand.brokerName = "brokerabc";
        when(artemisConnector.queueNames()).thenReturn(Arrays.asList("DLQ", "ExpiryQueue"));

        listQueuesCommand.run(null);
        verify(artemisConnector).queueNames();
    }
}