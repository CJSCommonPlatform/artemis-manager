package uk.gov.justice.artemis.manager.connector;

import static java.util.Collections.singletonList;
import static javax.jms.Session.SESSION_TRANSACTED;
import static org.apache.activemq.artemis.api.jms.ActiveMQJMSClient.createTopic;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.artemis.manager.util.JmsTestUtil.cleanQueue;
import static uk.gov.justice.artemis.manager.util.JmsTestUtil.cleanTopic;
import static uk.gov.justice.artemis.manager.util.JmsTestUtil.closeJmsConnection;
import static uk.gov.justice.artemis.manager.util.JmsTestUtil.openJmsConnection;
import static uk.gov.justice.artemis.manager.util.JmsTestUtil.putOnTopic;

import java.net.MalformedURLException;
import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import com.google.common.collect.ImmutableList;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DuplicateMessageIdsIT {

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
    public void shouldReprocessTwoMessagesOnDlqWithSameMessageIdOntoTopic() throws Exception {
        final String dlq = "DLQ";
        final String topicName = "Topic01";
        final String subscription_1 = "subscription01";
        final String subscription_2 = "subscription02";

        cleanQueue(dlq);
        cleanTopic(topicName, subscription_1);
        cleanTopic(topicName, subscription_2);

        final JmsTopicListener jmsTopicListener_1 = new JmsTopicListener(topicName, subscription_1);
        final JmsTopicListener jmsTopicListener_2 = new JmsTopicListener(topicName, subscription_2);
        final Thread topicListenerThread_1 = new Thread(jmsTopicListener_1);
        final Thread topicListenerThread_2 = new Thread(jmsTopicListener_2);

        topicListenerThread_1.start();
        topicListenerThread_2.start();

        Thread.sleep(1000L);

        putOnTopic(topicName, "{\"key1\":\"value123\"}");

        Thread.sleep(5000L);

        final List<MessageData> messageData = combinedArtemisConnector.messagesOf(dlq);

        messageData.forEach(message -> System.out.println("ID = " + message.getMsgId() + "\n original destination = " + message.getOriginalDestination() + "\n content = " + message.getMsgContent()));

        assertThat(messageData.size(), is(2));

        final long reprocessedMessages = combinedArtemisConnector.reprocess(dlq, singletonList(messageData.get(0).getMsgId()).iterator());

        final List<MessageData> messageDataAfter = combinedArtemisConnector.messagesOf(dlq);

        assertThat(reprocessedMessages, is(1L));
        assertThat(messageDataAfter, is(empty()));
    }

    public class JmsTopicListener implements Runnable {

        private final ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616?clientID=artemis-manager");
        private final String topicName;
        private final String subscriptionName;

        public JmsTopicListener(final String topicName, final String subscriptionName) {
            this.topicName = topicName;
            this.subscriptionName = subscriptionName;
        }

        @Override
        public void run() {
            Connection connection = null;
            Session session = null;

            try {
                connection = connectionFactory.createConnection();
                session = connection.createSession(true, SESSION_TRANSACTED);
                connection.start();
                final MessageConsumer subscriber = session.createConsumer(createTopic(topicName));

                Message message = subscriber.receive(5000);
                while (message != null) {
                    System.out.println("Subscription name: " + subscriptionName + " Message: " + message.getJMSMessageID() + " redelivered " + message.getJMSRedelivered() + " counter " + message.getObjectProperty("JMSXDeliveryCount"));
                    session.rollback();
                    message = subscriber.receive(2000);
                }

            } catch (final JMSException e) {
                e.printStackTrace();
            } finally {
                try {

                    if (null != connection) {
                        connection.stop();

                        if (session != null) {
                            session.close();
                        }

                        connection.close();
                    }

                } catch (final JMSException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
