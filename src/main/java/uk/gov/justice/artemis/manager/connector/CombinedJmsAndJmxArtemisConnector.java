package uk.gov.justice.artemis.manager.connector;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.management.remote.JMXConnector;

import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.api.jms.management.JMSQueueControl;
import org.apache.activemq.artemis.jms.client.ActiveMQQueueConnectionFactory;

/**
 * reprocess, remove and messagesOf were re-implemented in JMS due to issues with large messages over JMX.
 *
 */
public class CombinedJmsAndJmxArtemisConnector extends JmxArtemisConnector {

    private static final String JMS_URL = "tcp://%s:%s";
    private static final String JMS_ORIGINAL_DESTINATION = "_AMQ_ORIG_ADDRESS";
    private static final String ID_PREFIX = "ID:";
    private static final String BLANK = "";
    private static final String UNSUPPORTED_MESSAGE_CONTENT = "{\"error\": \"Unsupported message content\"}";

    private Function<JMSQueueControl, Function<Iterator<String>, Long>> removeMessages = queueControl -> msgIds -> {
        long removedMessages = 0L;

        while (msgIds.hasNext()) {
            try {
                queueControl.removeMessage(format("ID:%s", msgIds.next()));
                removedMessages++;
            } catch (final Exception exception) {
                outputPrinter.writeException(exception);
            }
        }

        return removedMessages;
    };

    private Function<JMSQueueControl, Function<Iterator<String>, Long>> reprocessMessages = queueControl -> msgIds -> {
        long reprocessedMessages = 0L;

        while (msgIds.hasNext()) {
            try {
                final String nextId = msgIds.next();
                if (queueControl.retryMessage(format("ID:%s", nextId))) {
                    reprocessedMessages++;
                } else {
                    outputPrinter.writeException(new RuntimeException(format("Skipped retrying of message id %s as it does not exist", nextId)));
                }
            } catch (final Exception exception) {
                outputPrinter.writeException(exception);
            }
        }
        return reprocessedMessages;
    };


    @Override
    public List<MessageData> messagesOf(final String host, final String port, final String brokerName, final String destinationName) throws Exception {

        final Queue queue = ActiveMQJMSClient.createQueue(destinationName);

        try (final ActiveMQQueueConnectionFactory connectionFactory = new ActiveMQQueueConnectionFactory(format(JMS_URL, host, port));
             final QueueConnection queueConnection = connectionFactory.createQueueConnection();
             final QueueSession queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
             final QueueBrowser queueBrowser = queueSession.createBrowser(queue)) {

            final Enumeration browserEnumeration = queueBrowser.getEnumeration();

            final ArrayList<MessageData> messages = new ArrayList<>();

            while (browserEnumeration.hasMoreElements()) {
                final Message message = (Message) browserEnumeration.nextElement();

                final String jmsMessageID = message.getJMSMessageID().replaceFirst(ID_PREFIX, BLANK);
                final String originalDestination = message.getStringProperty(JMS_ORIGINAL_DESTINATION);
                final String text;

                if (message instanceof TextMessage) {
                    final TextMessage textMessage = (TextMessage) message;
                    text = textMessage.getText();
                } else {
                    text = UNSUPPORTED_MESSAGE_CONTENT;
                }

                messages.add(new MessageData(jmsMessageID, originalDestination, text));
            }

            return messages;
        }
    }

    @Override
    public long remove(final String host, final String port, final String brokerName, final String destinationName, final Iterator<String> msgIds) throws Exception {
        return processJmxFunction(host, port, brokerName, destinationName, msgIds, removeMessages);
    }

    @Override
    public long reprocess(final String host, final String port, final String brokerName, final String destinationName, final Iterator<String> msgIds) throws Exception {
        return processJmxFunction(host, port, brokerName, destinationName, msgIds, reprocessMessages);
    }

    private long processJmxFunction(final String host, final String port, final String brokerName, final String destinationName, final Iterator<String> msgIds, final Function<JMSQueueControl, Function<Iterator<String>, Long>> processMessages) throws Exception {
        try (final JMXConnector connector = getJMXConnector(host, port)) {
            final JMSQueueControl queueControl = queueControlOf(connector, brokerName, destinationName);

            return processMessages.apply(queueControl).apply(msgIds);
        }
    }
}