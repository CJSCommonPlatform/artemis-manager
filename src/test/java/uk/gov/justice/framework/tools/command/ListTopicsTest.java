package uk.gov.justice.framework.tools.command;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.artemis.manager.connector.ArtemisConnector;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class ListTopicsTest {

    @Mock
    private ArtemisConnector artemisConnector;

    @InjectMocks
    private ListTopics listTopicsCommand;

    @Test
    public void shouldInvokeConnector() throws Exception {
        listTopicsCommand.jmxURLs = singletonList("service:jmx:rmi:///jndi/rmi://localhost:3000/jmxrmi");
        listTopicsCommand.brokerName = "brokerabc";
        when(artemisConnector.topicNames()).thenReturn(singletonList("testTopic"));

        listTopicsCommand.run(null);
        verify(artemisConnector).topicNames();
    }
}