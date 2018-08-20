package uk.gov.justice.framework.tools.command;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import uk.gov.justice.artemis.manager.connector.ArtemisConnector;


@RunWith(MockitoJUnitRunner.class)
public class QueueMessageCountsTest {

    @Mock
    ArtemisConnector artemisConnector;

    @InjectMocks
    QueueMessageCounts queueMessageCounts;

    @Test
    public void shouldInvokeConnector() throws Exception {
        queueMessageCounts.brokerName = "brokerabc";
        queueMessageCounts.host = "some.host";
        queueMessageCounts.port = "1212";
        final String[] queues = {"queueA", "queueB" };
        final Map<String, Long> messageCounts = Collections.unmodifiableMap(new HashMap<String, Long>() {
            {
                put("queueA", 100L);
                put("queueB", 101L);
            }
        });
        when(artemisConnector.queueNames(anyString(), anyString(), anyString())).thenReturn(queues);
        when(artemisConnector.queueMessageCount(anyString(), anyString(), anyString(), eq(queues) )).thenReturn(messageCounts);

        queueMessageCounts.run(null);
        verify(artemisConnector).queueMessageCount("some.host", "1212", "brokerabc", queues);
    }
}