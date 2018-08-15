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
public class ListTopicsTest {

    @Mock
    ArtemisConnector artemisConnector;

    @InjectMocks
    ListTopics listTopicsCommand;

    @Test
    public void shouldInvokeConnector() throws Exception {
        listTopicsCommand.brokerName = "brokerabc";
        listTopicsCommand.host = "some.host";
        listTopicsCommand.port = "1212";
        when(artemisConnector.topicNames(anyString(), anyString(), anyString())).thenReturn(new String[] {"testTopic" });

        listTopicsCommand.run(null);
        verify(artemisConnector).topicNames("some.host", "1212", "brokerabc");
    }
}