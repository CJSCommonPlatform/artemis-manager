package uk.gov.justice.artemis.manager.util;

import static org.apache.activemq.artemis.api.jms.ActiveMQJMSClient.createQueue;
import static org.apache.activemq.artemis.api.jms.ActiveMQJMSClient.createTopic;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import org.apache.activemq.artemis.jms.client.ActiveMQTopicConnectionFactory;

public class JmsTestUtil {
    private static final TopicConnectionFactory JMS_CF = new ActiveMQTopicConnectionFactory("tcp://localhost:61616");
    private static TopicConnection JMS_CONNECTION;
    private static TopicSession JMS_SESSION;
    private static Map<String, Queue> QUEUES = new HashMap<>();
    private static Map<String, MessageConsumer> CONSUMERS = new HashMap<>();
    private static Map<String, MessageProducer> PRODUCERS = new HashMap<>();
    private static Map<String, Topic> TOPICS = new HashMap<>();
    private static Map<String, TopicSubscriber> SUBSCRIBERS = new HashMap<>();
    private static Map<String, TopicPublisher> PUBLISHERS = new HashMap<>();

    public static void putInQueue(final String queueName, final String msgText, final String... origAddress) throws JMSException {
        TextMessage message = JMS_SESSION.createTextMessage(msgText);
        if (origAddress.length > 0) {
            message.setStringProperty("_AMQ_ORIG_ADDRESS", origAddress[0]);
        }
        producerOf(queueName).send(message);
    }

    public static void putInQueue(final String queueName, final InputStream messageInput, final String... origAddress) throws JMSException {
        final Message message = JMS_SESSION.createBytesMessage();

        message.setObjectProperty("JMS_AMQ_InputStream", messageInput);

        if (origAddress.length > 0) {
            message.setStringProperty("_AMQ_ORIG_ADDRESS", origAddress[0]);
        }

        producerOf(queueName).send(message);
    }

    public static void putOnTopic(final String topicName, final String msgText, final String... origAddress) throws JMSException {
       TextMessage message = JMS_SESSION.createTextMessage(msgText);
       if (origAddress.length > 0) {
           message.setStringProperty("_AMQ_ORIG_ADDRESS", origAddress[0]);
       }
       publisherOf(topicName).publish(message);
    }

    /**
     * Returns the number of messages that were removed from the queue.
     *
     * @param queueName - the name of the queue that is to be cleaned
     * @return the number of cleaned messages
     */
    public static int cleanQueue(final String queueName) throws JMSException {
        JMS_CONNECTION.start();
        final MessageConsumer consumer = consumerOf(queueName);

        int cleanedMessage = 0;
        while (consumer.receiveNoWait() != null) {
            cleanedMessage++;
        }
        JMS_CONNECTION.stop();
        return cleanedMessage;
    }

    /**
     * Returns the number of messages that were received from the topic.
     *
     * @param topicName - the name of the topic that is to be cleaned
     * @return the number of cleaned messages
     */
    public static int cleanTopic(final String topicName) throws JMSException {
        JMS_CONNECTION.start();
        final MessageConsumer subscriber = subscriberOf(topicName);

        int cleanedMessage = 0;
        while (subscriber.receiveNoWait() != null) {
            cleanedMessage++;
        }
        JMS_CONNECTION.stop();
        return cleanedMessage;
    }

    public static MessageConsumer consumerOf(final String queueName) throws JMSException {
        return CONSUMERS.computeIfAbsent(queueName, name -> {
            try {
                return JMS_SESSION.createConsumer(queueOf(name));
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static MessageProducer producerOf(final String queueName) throws JMSException {
        return PRODUCERS.computeIfAbsent(queueName, name -> {
            try {
                return JMS_SESSION.createProducer(queueOf(name));
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static TopicPublisher publisherOf(final String topicName) throws JMSException {
        return PUBLISHERS.computeIfAbsent(topicName,  name -> {
            try {
                return JMS_SESSION.createPublisher(topicOf(name));
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static TopicSubscriber subscriberOf(final String topicName) throws JMSException {
        return SUBSCRIBERS.computeIfAbsent(topicName,  name -> {
            try {
                return JMS_SESSION.createSubscriber(topicOf(name));
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    public static void closeJmsConnection() throws JMSException {
        SUBSCRIBERS.clear();
        PUBLISHERS.clear();

        CONSUMERS.clear();
        PRODUCERS.clear();

        TOPICS.clear();
        QUEUES.clear();

        JMS_CONNECTION.close();
    }

    public static void openJmsConnection() throws JMSException {
        JMS_CF.createConnection();
        JMS_CONNECTION = JMS_CF.createTopicConnection();
        JMS_SESSION = JMS_CONNECTION.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    private static Queue queueOf(final String queueName) {
        return QUEUES.computeIfAbsent(queueName, name -> createQueue(queueName));
    }

    private static Topic topicOf(final String topicName) {
        return TOPICS.computeIfAbsent(topicName, name -> createTopic(topicName));
    }
}
