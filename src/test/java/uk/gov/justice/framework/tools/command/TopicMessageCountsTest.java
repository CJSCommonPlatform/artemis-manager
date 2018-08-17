package uk.gov.justice.framework.tools.command;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
public class TopicMessageCountsTest {

    @Mock
    ArtemisConnector artemisConnector;

    @InjectMocks
    TopicMessageCounts topicMessageCounts;

    @Test
    public void shouldInvokeConnector() throws Exception {
        topicMessageCounts.brokerName = "brokerabc";
        topicMessageCounts.host = "some.host";
        topicMessageCounts.port = "1212";
        final String[] topics = {"topicA", "topicB" };
        final Map<String, Long> messageCounts = Collections.unmodifiableMap(new HashMap<String, Long>() {
            {
                put("topicA", 100L);
                put("topicB", 101L);
            }
        });
        when(artemisConnector.topicNames(anyString(), anyString(), anyString())).thenReturn(topics);
        when(artemisConnector.topicMessageCount(anyString(), anyString(), anyString(), eq(topics) )).thenReturn(messageCounts);

        topicMessageCounts.run(null);
        Map<String, Long> results = verify(artemisConnector).topicMessageCount("some.host", "1212", "brokerabc", topics);
    }
}