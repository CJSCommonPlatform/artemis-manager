package uk.gov.justice.artemis.manager.connector;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static javax.management.MBeanServerInvocationHandler.newProxyInstance;
import static javax.management.remote.JMXConnectorFactory.connect;
import static org.apache.activemq.artemis.api.config.ActiveMQDefaultConfiguration.getDefaultJmxDomain;

import uk.gov.justice.output.ConsolePrinter;
import uk.gov.justice.output.OutputPrinter;

import java.util.Iterator;
import java.util.List;

import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;

import org.apache.activemq.artemis.api.core.management.ObjectNameBuilder;
import org.apache.activemq.artemis.api.jms.management.JMSQueueControl;

public class JmxArtemisConnector implements ArtemisConnector {

    private static final String JMX_URL = "service:jmx:rmi:///jndi/rmi://%s:%s/jmxrmi";
    private static final String JMS_MESSAGE_ID = "JMSMessageID";
    private static final String ORIGINAL_DESTINATION = "OriginalDestination";
    private static final String TEXT = "Text";

    final OutputPrinter outputPrinter = new ConsolePrinter();

    @Override
    public List<MessageData> messagesOf(final String host, final String port, final String brokerName, final String destinationName) throws Exception {
        final CompositeData[] browseResult = queueControlOf(host, port, brokerName, destinationName).browse();
        return stream(browseResult)
                .map(cd -> new MessageData(String.valueOf(cd.get(JMS_MESSAGE_ID)).replaceFirst("ID:", ""), String.valueOf(cd.get(ORIGINAL_DESTINATION)), String.valueOf(cd.get(TEXT))))
                .collect(toList());

    }

    @Override
    public long remove(final String host, final String port, final String brokerName, final String destinationName, final Iterator<String> msgIds) throws Exception {
        final JMSQueueControl queueControl = queueControlOf(host, port, brokerName, destinationName);
        long removedMessages = 0;
        while (msgIds.hasNext()) {
            try {
                queueControl.removeMessage(format("ID:%s", msgIds.next()));
                removedMessages++;
            } catch (final IllegalArgumentException exception) {
                outputPrinter.writeException(exception);
            }
        }
        return removedMessages;
    }

    @Override
    public long reprocess(final String host, final String port, final String brokerName, final String destinationName, final Iterator<String> msgIds) throws Exception {
        final JMSQueueControl queueControl = queueControlOf(host, port, brokerName, destinationName);
        long reprocessedMessages = 0;
        while (msgIds.hasNext()) {
            try {
                final String nextId = msgIds.next();
                if (queueControl.retryMessage(format("ID:%s", nextId))) {
                    reprocessedMessages++;
                } else {
                    outputPrinter.writeException(new RuntimeException(format("Skipped retrying of message id %s as it does not exist", nextId)));
                }
            } catch (final IllegalArgumentException exception) {
                outputPrinter.writeException(exception);
            }
        }
        return reprocessedMessages;
    }

    private JMSQueueControl queueControlOf(final String host, final String port, final String brokerName, final String destinationName) throws Exception {
        final ObjectName on = ObjectNameBuilder.create(getDefaultJmxDomain(), brokerName, true).getJMSQueueObjectName(destinationName);
        final JMXConnector connector = connect(new JMXServiceURL(format(JMX_URL, host, port)), emptyMap());
        return newProxyInstance(connector.getMBeanServerConnection(), on, JMSQueueControl.class, false);
    }
}
