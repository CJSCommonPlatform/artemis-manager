package uk.gov.justice.artemis.manager.connector;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
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

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;

import javax.jms.JMSException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CombinedJmsAndJmxArtemisConnectorIT {

    private ArtemisConnector combinedArtemisConnector;

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
        this.combinedArtemisConnector = new CombinedJmsAndJmxArtemisConnector();
        this.combinedArtemisConnector.setParameters(
          Arrays.asList("service:jmx:rmi://localhost:3000/jndi/rmi://localhost:3000/jmxrmi"),
          "0.0.0.0",
          null,
          null,
          "tcp://localhost:61616?clientID=artemis-manager",
          null,
          null
        );
    }

    @Test
    public void shouldReturnMessagesFromQueue() throws Exception {
        final String queue = "DLQ";

        cleanQueue(queue);

        putInQueue(queue, "{\"key1\":\"value123\"}", "origQueueO1");
        putInQueue(queue, "{\"key1\":\"valueBB\"}", "origQueueO2");

        final List<MessageData> messageData = combinedArtemisConnector.messagesOf(queue);
        assertThat(messageData, hasSize(2));
        assertThat(messageData.get(0).getMsgId(), not(nullValue()));
        assertThat(messageData.get(0).getOriginalDestination(), is("origQueueO1"));
        assertThat(messageData.get(0).getMsgContent().getString("key1"), is("value123"));

        assertThat(messageData.get(1).getMsgId(), not(nullValue()));
        assertThat(messageData.get(1).getOriginalDestination(), is("origQueueO2"));
        assertThat(messageData.get(1).getMsgContent().getString("key1"), is("valueBB"));
    }

    @Test
    public void shouldReturnUnsupportedMessageTextForByteMessage() throws Exception {
        final String queue = "DLQ";

        cleanQueue(queue);

        final ByteArrayInputStream messageInput = new ByteArrayInputStream("{\"key1\":\"value123\"}".getBytes());

        putInQueue(queue, messageInput, "origQueueO1");

        final List<MessageData> messageData = combinedArtemisConnector.messagesOf(queue);
        assertThat(messageData, hasSize(1));
        assertThat(messageData.get(0).getMsgId(), not(nullValue()));
        assertThat(messageData.get(0).getOriginalDestination(), is("origQueueO1"));
        assertThat(messageData.get(0).getMsgContent().getString("error"), is("Unsupported message content"));
    }

    @Test
    public void shouldRemoveMessagesFromQueue() throws Exception {
        final String queue = "DLQ";

        cleanQueue(queue);

        putInQueue(queue, "{\"key1\":\"value123\"}", "origQueueO1");
        putInQueue(queue, "{\"key1\":\"valueBB\"}", "origQueueO2");
        putInQueue(queue, "{\"key1\":\"valueCC\"}", "origQueueO3");

        final List<MessageData> messageData = combinedArtemisConnector.messagesOf(queue);
        assertThat(messageData, hasSize(3));

        combinedArtemisConnector.remove(queue, asList(messageData.get(1).getMsgId(), messageData.get(2).getMsgId()).iterator());

        final List<MessageData> messageDataAfterRemoval = combinedArtemisConnector.messagesOf(queue);
        assertThat(messageDataAfterRemoval, hasSize(1));

        assertThat(messageDataAfterRemoval.get(0).getMsgId(), is(messageData.get(0).getMsgId()));
    }

    @Test
    public void shouldIgnoreMessagesNotInTheQueue() throws Exception {
        final String queue = "DLQ";

        cleanQueue(queue);

        putInQueue(queue, "{\"key1\":\"value123\"}", "origQueueO1");
        putInQueue(queue, "{\"key1\":\"valueBB\"}", "origQueueO2");

        final List<MessageData> messageData = combinedArtemisConnector.messagesOf(queue);
        assertThat(messageData, hasSize(2));

        combinedArtemisConnector.remove(queue, asList("id_does_not_exist_123", messageData.get(1).getMsgId()).iterator());

        final List<MessageData> messageDataAfterRemoval = combinedArtemisConnector.messagesOf(queue);
        assertThat(messageDataAfterRemoval, hasSize(1));

        assertThat(messageDataAfterRemoval.get(0).getMsgId(), is(messageData.get(0).getMsgId()));
    }

    @Test
    public void shouldReturnNumberOfDeletedMessages() throws Exception {
        final String queue = "DLQ";

        cleanQueue(queue);

        putInQueue(queue, "{\"key1\":\"value123\"}", "origQueueO1");
        putInQueue(queue, "{\"key1\":\"valueBB\"}", "origQueueO2");
        putInQueue(queue, "{\"key1\":\"valueCC\"}", "origQueueO3");

        final List<MessageData> messageData = combinedArtemisConnector.messagesOf(queue);

        final long removedMessages = combinedArtemisConnector.remove(queue, asList(messageData.get(1).getMsgId(), "unknown_id", messageData.get(2).getMsgId()).iterator());
        assertThat(removedMessages, is(2L));
    }

    @Test
    public void shouldReprocessMessageOntoOriginalQueue() throws Exception {
        final String queue = "DLQ";

        cleanQueue(queue);

        putInQueue(queue, "{\"key1\":\"value123\"}", "origQueueO1");
        putInQueue(queue, "{\"key1\":\"valueBB\"}", "origQueueO2");

        final List<MessageData> messageData = combinedArtemisConnector.messagesOf(queue);

        final long reprocessedMessages = combinedArtemisConnector.reprocess(queue, asList(messageData.get(0).getMsgId(), messageData.get(1).getMsgId()).iterator());

        final List<MessageData> messageDataAfter = combinedArtemisConnector.messagesOf(queue);

        assertThat(reprocessedMessages, is(2L));
        assertThat(messageDataAfter, is(empty()));
    }

    @Test
    public void shouldReturnListOfQueues() throws Exception {
        final List<String> queueNames = combinedArtemisConnector.queueNames();
        assertThat(queueNames, contains("DLQ", "ExpiryQueue"));
    }

    @Test
    public void shouldReturnListOfTopics() throws Exception {
        final String topic = "testTopic";

        cleanTopic(topic, "testSubscription");


        final List<String> topicNames = combinedArtemisConnector.topicNames();

        assertThat(topicNames, contains("testTopic"));
    }
}