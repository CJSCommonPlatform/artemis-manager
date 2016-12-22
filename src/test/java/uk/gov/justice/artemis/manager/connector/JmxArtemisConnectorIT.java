package uk.gov.justice.artemis.manager.connector;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.artemis.manager.util.JmsTestUtil.cleanQueue;
import static uk.gov.justice.artemis.manager.util.JmsTestUtil.closeJmsConnection;
import static uk.gov.justice.artemis.manager.util.JmsTestUtil.openJmsConnection;
import static uk.gov.justice.artemis.manager.util.JmsTestUtil.putInQueue;

import java.util.List;

import javax.jms.JMSException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

//to run this test from IDE start artemis first by executing ./target/server0/bin/artemis run
public class JmxArtemisConnectorIT {

    private ArtemisConnector jmxArtemisConnector = new JmxArtemisConnector();

    @BeforeClass
    public static void beforeClass() throws JMSException {
        openJmsConnection();
    }

    @AfterClass
    public static void afterClass() throws JMSException {
        closeJmsConnection();
    }

    @Test
    public void shouldReturnMessagesFromQueue() throws Exception {
        final String queue = "DLQ";

        cleanQueue(queue);

        putInQueue(queue, "{\"key1\":\"value123\"}", "origQueueO1");
        putInQueue(queue, "{\"key1\":\"valueBB\"}", "origQueueO2");

        final List<MessageData> messageData = jmxArtemisConnector.messagesOf("localhost", "3000", "0.0.0.0", queue);
        assertThat(messageData, hasSize(2));
        assertThat(messageData.get(0).getMsgId(), not(nullValue()));
        assertThat(messageData.get(0).getOriginalDestination(), is("origQueueO1"));
        assertThat(messageData.get(0).getMsgContent().getString("key1"), is("value123"));

        assertThat(messageData.get(1).getMsgId(), not(nullValue()));
        assertThat(messageData.get(1).getOriginalDestination(), is("origQueueO2"));
        assertThat(messageData.get(1).getMsgContent().getString("key1"), is("valueBB"));
    }

    @Test
    public void shouldRemoveMessagesFromQueue() throws Exception {
        final String queue = "DLQ";

        cleanQueue(queue);

        putInQueue(queue, "{\"key1\":\"value123\"}", "origQueueO1");
        putInQueue(queue, "{\"key1\":\"valueBB\"}", "origQueueO2");
        putInQueue(queue, "{\"key1\":\"valueCC\"}", "origQueueO3");

        final List<MessageData> messageData = jmxArtemisConnector.messagesOf("localhost", "3000", "0.0.0.0", queue);
        assertThat(messageData, hasSize(3));

        jmxArtemisConnector.remove("localhost", "3000", "0.0.0.0", queue, asList(messageData.get(1).getMsgId(), messageData.get(2).getMsgId()).iterator());

        final List<MessageData> messageDataAfterRemoval = jmxArtemisConnector.messagesOf("localhost", "3000", "0.0.0.0", queue);
        assertThat(messageDataAfterRemoval, hasSize(1));

        assertThat(messageDataAfterRemoval.get(0).getMsgId(), is(messageData.get(0).getMsgId()));
    }

    @Test
    public void shouldIgnoreMessagesNotInTheQueue() throws Exception {
        final String queue = "DLQ";

        cleanQueue(queue);

        putInQueue(queue, "{\"key1\":\"value123\"}", "origQueueO1");
        putInQueue(queue, "{\"key1\":\"valueBB\"}", "origQueueO2");

        final List<MessageData> messageData = jmxArtemisConnector.messagesOf("localhost", "3000", "0.0.0.0", queue);
        assertThat(messageData, hasSize(2));

        jmxArtemisConnector.remove("localhost", "3000", "0.0.0.0", queue, asList("id_does_not_exist_123", messageData.get(1).getMsgId()).iterator());

        final List<MessageData> messageDataAfterRemoval = jmxArtemisConnector.messagesOf("localhost", "3000", "0.0.0.0", queue);
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

        final List<MessageData> messageData = jmxArtemisConnector.messagesOf("localhost", "3000", "0.0.0.0", queue);

        final long removedMessages = jmxArtemisConnector.remove("localhost", "3000", "0.0.0.0", queue, asList(messageData.get(1).getMsgId(), "unknown_id", messageData.get(2).getMsgId()).iterator());
        assertThat(removedMessages, is(2L));

    }


}