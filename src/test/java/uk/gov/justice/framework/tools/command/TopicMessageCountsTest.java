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
public class TopicMessageCountsTest {

    @Mock
    private ArtemisConnector artemisConnector;

    @InjectMocks
    private TopicMessageCounts topicMessageCounts;

    @Test
    public void shouldInvokeConnector() throws Exception {
        topicMessageCounts.jmxURLs = singletonList("service:jmx:rmi:///jndi/rmi://localhost:3000/jmxrmi");
        topicMessageCounts.brokerName = "brokerabc";
        final List<String> topics = Arrays.asList("topicA", "topicB");
        final Map<String, Long> messageCounts = Collections.unmodifiableMap(new HashMap<String, Long>() {
            {
                put("topicA", 100L);
                put("topicB", 101L);
            }
        });
        when(artemisConnector.topicNames()).thenReturn(topics);
        when(artemisConnector.topicMessageCount(eq(topics))).thenReturn(messageCounts);

        topicMessageCounts.run(null);
        Map<String, Long> results = verify(artemisConnector).topicMessageCount(topics);
    }
}