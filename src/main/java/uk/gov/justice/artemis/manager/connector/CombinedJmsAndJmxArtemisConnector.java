package uk.gov.justice.artemis.manager.connector;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static javax.management.MBeanServerInvocationHandler.newProxyInstance;
import static javax.management.remote.JMXConnectorFactory.connect;
import static org.apache.activemq.artemis.api.config.ActiveMQDefaultConfiguration.getDefaultJmxDomain;

import uk.gov.justice.output.ConsolePrinter;
import uk.gov.justice.output.OutputPrinter;

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
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;

import org.apache.activemq.artemis.api.core.management.ObjectNameBuilder;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.api.jms.management.JMSQueueControl;
import org.apache.activemq.artemis.jms.client.ActiveMQQueueConnectionFactory;

public class CombinedJmsAndJmxArtemisConnector implements ArtemisConnector {

    private static final String JMS_URL = "tcp://%s:%s";
    private static final String JMS_ORIGINAL_DESTINATION = "_AMQ_ORIG_ADDRESS";
    private static final String JMX_URL = "service:jmx:rmi:///jndi/rmi://%s:%s/jmxrmi";
    private static final String ID_PREFIX = "ID:";
    private static final String BLANK = "";
    private static final String UNSUPPORTED_MESSAGE_CONTENT = "{\"error\": \"Unsupported message content\"}";
    private static final String JMS_CONSUMER = "_AMQ_ORIG_QUEUE";

    final OutputPrinter outputPrinter = new ConsolePrinter();

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
                final String consumer = message.getStringProperty(JMS_CONSUMER);

                if (message instanceof TextMessage) {
                    final TextMessage textMessage = (TextMessage) message;
                    text = textMessage.getText();
                } else {
                    text = UNSUPPORTED_MESSAGE_CONTENT;
                }

                messages.add(new MessageData(jmsMessageID, originalDestination, text, consumer));
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
        final ObjectName on = ObjectNameBuilder.create(getDefaultJmxDomain(), brokerName, true).getJMSQueueObjectName(destinationName);

        try (final JMXConnector connector = connect(new JMXServiceURL(format(JMX_URL, host, port)), emptyMap())) {
            final JMSQueueControl queueControl = newProxyInstance(connector.getMBeanServerConnection(), on, JMSQueueControl.class, false);

            return processMessages.apply(queueControl).apply(msgIds);
        }
    }
}
