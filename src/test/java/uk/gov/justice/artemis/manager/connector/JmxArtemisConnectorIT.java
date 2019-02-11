package uk.gov.justice.artemis.manager.connector;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.artemis.manager.util.JmsTestUtil.cleanQueue;
import static uk.gov.justice.artemis.manager.util.JmsTestUtil.cleanTopic;
import static uk.gov.justice.artemis.manager.util.JmsTestUtil.closeJmsConnection;
import static uk.gov.justice.artemis.manager.util.JmsTestUtil.openJmsConnection;
import static uk.gov.justice.artemis.manager.util.JmsTestUtil.putInQueue;
import static uk.gov.justice.artemis.manager.util.JmsTestUtil.putOnTopic;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//to run this test from IDE start artemis first by executing ./target/server0/bin/artemis run
public class JmxArtemisConnectorIT {

    private Logger logger = LoggerFactory.getLogger(JmxArtemisConnectorIT.class);
    private ArtemisConnector jmxArtemisConnector;

    @BeforeClass
    public static void beforeClass() throws JMSException {
        openJmsConnection();
    }

    @AfterClass
    public static void afterClass() throws JMSException {
        closeJmsConnection();
    }

    @Before
    public void setUp() throws MalformedURLException {
        this.jmxArtemisConnector = new JmxArtemisConnector();
        this.jmxArtemisConnector.setParameters(
            asList("service:jmx:rmi:///jndi/rmi://localhost:3000/jmxrmi"),
            "0.0.0.0",
            null,
            null,
            null,
            null,
            null
          );
    }

    @Test
    public void shouldReturnMessagesFromQueue() throws Exception {
        final String queue = "DLQ";

        cleanQueue(queue);

        putInQueue(queue, "{\"key1\":\"value123\"}", "consumer1","origQueueO1");
        putInQueue(queue, "{\"key1\":\"valueBB\"}", "consumer2", "origQueueO2");

        final List<MessageData> messageData = jmxArtemisConnector.messagesOf(queue);
        assertThat(messageData, hasSize(2));
        assertThat(messageData.get(0).getMsgId(), not(nullValue()));
        assertThat(messageData.get(0).getOriginalDestination(), is("origQueueO1"));
        assertThat(messageData.get(0).getMsgContent().getString("key1"), is("value123"));
        assertThat(messageData.get(0).getConsumer(), is("consumer1"));

        assertThat(messageData.get(1).getMsgId(), not(nullValue()));
        assertThat(messageData.get(1).getOriginalDestination(), is("origQueueO2"));
        assertThat(messageData.get(1).getMsgContent().getString("key1"), is("valueBB"));
        assertThat(messageData.get(1).getConsumer(), is("consumer2"));
    }

    @Test
    public void shouldRemoveMessagesFromQueue() throws Exception {
        final String queue = "DLQ";

        cleanQueue(queue);

        putInQueue(queue, "{\"key1\":\"value123\"}", "consumer1", "origQueueO1");
        putInQueue(queue, "{\"key1\":\"valueBB\"}", "consumer2","origQueueO2");
        putInQueue(queue, "{\"key1\":\"valueCC\"}", "consumer3","origQueueO3");

        final List<MessageData> messageData = jmxArtemisConnector.messagesOf(queue);
        assertThat(messageData, hasSize(3));

        jmxArtemisConnector.remove(queue, asList(messageData.get(0).getMsgId(), messageData.get(2).getMsgId()).iterator());

        final List<MessageData> messageDataAfterRemoval = jmxArtemisConnector.messagesOf(queue);
        assertThat(messageDataAfterRemoval, hasSize(1));

        assertThat(messageDataAfterRemoval.get(0).getMsgId(), is(messageData.get(1).getMsgId()));
    }

    @Test
    public void shouldIgnoreMessagesNotInTheQueue() throws Exception {
        final String queue = "DLQ";

        cleanQueue(queue);

        putInQueue(queue, "{\"key1\":\"value123\"}", "consumer1","origQueueO1");
        putInQueue(queue, "{\"key1\":\"valueBB\"}", "consumer2","origQueueO2");

        final List<MessageData> messageData = jmxArtemisConnector.messagesOf(queue);
        assertThat(messageData, hasSize(2));

        jmxArtemisConnector.remove(queue, asList("id_does_not_exist_123", messageData.get(1).getMsgId()).iterator());

        final List<MessageData> messageDataAfterRemoval = jmxArtemisConnector.messagesOf(queue);
        assertThat(messageDataAfterRemoval, hasSize(1));

        assertThat(messageDataAfterRemoval.get(0).getMsgId(), is(messageData.get(0).getMsgId()));
    }

    @Test
    public void shouldReturnNumberOfDeletedMessages() throws Exception {
        final String queue = "DLQ";

        cleanQueue(queue);

        putInQueue(queue, "{\"key1\":\"value123\"}", "consumer1","origQueueO1");
        putInQueue(queue, "{\"key1\":\"valueBB\"}", "consumer2","origQueueO2");
        putInQueue(queue, "{\"key1\":\"valueCC\"}", "consumer3","origQueueO3");

        final List<MessageData> messageData = jmxArtemisConnector.messagesOf(queue);

        final long removedMessages = jmxArtemisConnector.remove(queue, asList(messageData.get(1).getMsgId(), "unknown_id", messageData.get(2).getMsgId()).iterator());
        assertThat(removedMessages, is(2L));
    }

    @Test
    public void shouldReturnListOfQueues() throws Exception {
        final List<String> queueNames = jmxArtemisConnector.queueNames();
        assertThat(queueNames, hasItems("DLQ", "ExpiryQueue"));
    }

    @Test
    public void shouldReturnListOfTopics() throws Exception {
        final String topic = "testTopic";
        cleanTopic(topic, "testSubscription");
        final List<String> topicNames = jmxArtemisConnector.topicNames();
        assertThat(topicNames, hasItems("testTopic"));
    }

    @Test
    public void shouldReturnQueuesAndCounts() throws Exception {
        final String queue = "DLQ";
        final List<String> queues = asList("DLQ", "ExpiryQueue");

        try {
            cleanQueue(queue);
    
            putInQueue(queue, "{\"key1\":\"value123\"}", "origQueueO1");
            putInQueue(queue, "{\"key1\":\"valueBB\"}", "origQueueO2");
            putInQueue(queue, "{\"key1\":\"valueCC\"}", "origQueueO3");
    
            final Map<String,Long> results = jmxArtemisConnector.queueMessageCount(queues);
    
            assertThat(results.containsKey("DLQ"), is(true));
            assertThat(results.get("DLQ"),equalTo(3L));
        } catch ( final Exception e ) {
            logger.error("exception:", e);
            throw e;
        }
    }

    @Test
    public void shouldReturnTopicsAndCounts() throws Exception {
        final String topic = "testTopic";
        final List<String> topics = asList(topic);

        try {
            cleanTopic(topic, "testSubscription");
    
            putOnTopic(topic, "{\"key1\":\"value123\"}", "origQueueO1");
            putOnTopic(topic, "{\"key1\":\"valueBB\"}", "origQueueO2");
            putOnTopic(topic, "{\"key1\":\"valueCC\"}", "origQueueO3");

            final Map<String,Long> results = jmxArtemisConnector.topicMessageCount(topics);

            assertThat(results.containsKey("testTopic"), is(true));
            assertThat(results.get("testTopic"),equalTo(3L));
        } catch ( final Exception e ) {
            logger.error("exception:", e);
            throw e;
        }
    }
}