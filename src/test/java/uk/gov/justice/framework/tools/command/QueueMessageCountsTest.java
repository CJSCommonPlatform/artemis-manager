package uk.gov.justice.framework.tools.command;

import static java.util.Collections.singletonList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.artemis.manager.connector.ArtemisConnector;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class QueueMessageCountsTest {

    @Mock
    private ArtemisConnector artemisConnector;

    @InjectMocks
    private QueueMessageCounts queueMessageCounts;

    @Test
    public void shouldInvokeConnector() throws Exception {
        queueMessageCounts.jmxURLs = singletonList("service:jmx:rmi:///jndi/rmi://localhost:3000/jmxrmi");
        queueMessageCounts.brokerName = "brokerabc";
        final List<String> queues = Arrays.asList("queueA", "queueB");
        final Map<String, Long> messageCounts = Collections.unmodifiableMap(new HashMap<String, Long>() {
            {
                put("queueA", 100L);
                put("queueB", 101L);
            }
        });
        when(artemisConnector.queueNames()).thenReturn(queues);
        when(artemisConnector.queueMessageCount(eq(queues))).thenReturn(messageCounts);

        queueMessageCounts.run(null);
        verify(artemisConnector).queueMessageCount(queues);
    }
}