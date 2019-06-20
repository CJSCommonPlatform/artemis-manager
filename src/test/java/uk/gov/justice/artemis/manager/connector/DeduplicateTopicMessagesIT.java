package uk.gov.justice.artemis.manager.connector;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.artemis.manager.util.JmsTestUtil.cleanQueue;
import static uk.gov.justice.artemis.manager.util.JmsTestUtil.closeJmsConnection;
import static uk.gov.justice.artemis.manager.util.JmsTestUtil.openJmsConnection;
import static uk.gov.justice.artemis.manager.util.JmsTestUtil.putInQueueWithMessageId;

import java.net.MalformedURLException;
import java.util.List;

import javax.jms.JMSException;

import com.google.common.collect.ImmutableList;
import org.apache.activemq.artemis.utils.UUID;
import org.apache.activemq.artemis.utils.UUIDGenerator;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

//to run this test from IDE start artemis first by executing ./target/server0/bin/artemis run
public class DeduplicateTopicMessagesIT {

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
                ImmutableList.of("service:jmx:rmi://localhost:3000/jndi/rmi://localhost:3000/jmxrmi"),
                "0.0.0.0",
                null,
                null,
                "tcp://localhost:61616?clientID=artemis-manager",
                null,
                null
        );
    }

    @Test
    public void shouldRemoveMessageDuplicates() throws Exception {

        final UUIDGenerator uuidGenerator = UUIDGenerator.getInstance();

        final String queue = "DLQ";

        final UUID jmsMessageId_1 = uuidGenerator.generateUUID();
        final UUID jmsMessageId_2 = uuidGenerator.generateUUID();

        final String messageText = "{\"key1\":\"value123\"}";
        final String consumer_1 = "consumer1";
        final String consumer_2 = "consumer2";
        final String consumer_3 = "consumer3";
        final String originalQueue_1 = "origQueueO1";

        cleanQueue(queue);

        putInQueueWithMessageId(queue, jmsMessageId_1, messageText, consumer_1, originalQueue_1);
        putInQueueWithMessageId(queue, jmsMessageId_1, messageText, consumer_2, originalQueue_1);
        putInQueueWithMessageId(queue, jmsMessageId_1, messageText, consumer_3, originalQueue_1);
        putInQueueWithMessageId(queue, jmsMessageId_2, messageText, consumer_1, originalQueue_1);

        final List<MessageData> messageData_1 = combinedArtemisConnector.messagesOf(queue);

        assertThat(messageData_1, hasSize(4));
        assertThat(messageData_1.get(0).getMsgId(), is(jmsMessageId_1.toString()));
        assertThat(messageData_1.get(0).getOriginalDestination(), is(originalQueue_1));
        assertThat(messageData_1.get(0).getMsgContent().getString("key1"), is("value123"));
        assertThat(messageData_1.get(0).getConsumer(), is(consumer_1));

        assertThat(messageData_1.get(1).getMsgId(), is(jmsMessageId_1.toString()));
        assertThat(messageData_1.get(1).getOriginalDestination(), is(originalQueue_1));
        assertThat(messageData_1.get(1).getMsgContent().getString("key1"), is("value123"));
        assertThat(messageData_1.get(1).getConsumer(), is(consumer_2));

        assertThat(messageData_1.get(2).getMsgId(), is(jmsMessageId_1.toString()));
        assertThat(messageData_1.get(2).getOriginalDestination(), is(originalQueue_1));
        assertThat(messageData_1.get(2).getMsgContent().getString("key1"), is("value123"));
        assertThat(messageData_1.get(2).getConsumer(), is(consumer_3));

        assertThat(messageData_1.get(3).getMsgId(), is(jmsMessageId_2.toString()));
        assertThat(messageData_1.get(3).getOriginalDestination(), is(originalQueue_1));
        assertThat(messageData_1.get(3).getMsgContent().getString("key1"), is("value123"));
        assertThat(messageData_1.get(3).getConsumer(), is(consumer_1));

        final List<String> listOfMessageIds = combinedArtemisConnector.deduplicateTopicMessages(queue);

        assertThat(listOfMessageIds.size(), is(3));

        final List<MessageData> messageData_2 = combinedArtemisConnector.messagesOf(queue);

        assertThat(messageData_2, hasSize(4));
        assertThat(messageData_2.get(0).getMsgId(), is(jmsMessageId_2.toString()));
        assertThat(messageData_2.get(0).getOriginalDestination(), is(originalQueue_1));
        assertThat(messageData_2.get(0).getMsgContent().getString("key1"), is("value123"));
        assertThat(messageData_2.get(0).getConsumer(), is(consumer_1));

        assertThat(messageData_2.get(1).getMsgId(), is(listOfMessageIds.get(0)));
        assertThat(messageData_2.get(1).getOriginalDestination(), is(originalQueue_1));
        assertThat(messageData_2.get(1).getMsgContent().getString("key1"), is("value123"));
        assertThat(messageData_2.get(1).getConsumer(), is(consumer_1));

        assertThat(messageData_2.get(2).getMsgId(), is(listOfMessageIds.get(1)));
        assertThat(messageData_2.get(2).getOriginalDestination(), is(originalQueue_1));
        assertThat(messageData_2.get(2).getMsgContent().getString("key1"), is("value123"));
        assertThat(messageData_2.get(2).getConsumer(), is(consumer_2));

        assertThat(messageData_2.get(3).getMsgId(), is(listOfMessageIds.get(2)));
        assertThat(messageData_2.get(3).getOriginalDestination(), is(originalQueue_1));
        assertThat(messageData_2.get(3).getMsgContent().getString("key1"), is("value123"));
        assertThat(messageData_2.get(3).getConsumer(), is(consumer_3));
    }
}
