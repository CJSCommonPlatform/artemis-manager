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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;

import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;

import org.apache.activemq.artemis.api.core.management.ObjectNameBuilder;
import org.apache.activemq.artemis.api.jms.management.JMSQueueControl;
import org.apache.activemq.artemis.api.jms.management.JMSServerControl;

public class JmxArtemisConnector implements ArtemisConnector {

    final protected OutputPrinter outputPrinter = new ConsolePrinter();

    private static final String JMX_URL = "service:jmx:rmi:///jndi/rmi://%s:%s/jmxrmi";
    private static final String JMS_MESSAGE_ID = "JMSMessageID";
    private static final String ORIGINAL_DESTINATION = "OriginalDestination";
    private static final String TEXT = "Text";

    @Override
    public List<MessageData> messagesOf(final String host, final String port, final String brokerName, final String destinationName) throws Exception {
        try (final JMXConnector connector = getJMXConnector(host, port)) {
            final CompositeData[] browseResult = queueControlOf(connector, brokerName, destinationName).browse();
            return stream(browseResult)
                  .map(cd -> new MessageData(String.valueOf(cd.get(JMS_MESSAGE_ID)).replaceFirst("ID:", ""), String.valueOf(cd.get(ORIGINAL_DESTINATION)), String.valueOf(cd.get(TEXT))))
                  .collect(toList());
        }

    }

    @Override
    public long remove(final String host, final String port, final String brokerName, final String destinationName, final Iterator<String> msgIds) throws Exception {
        try (final JMXConnector connector = getJMXConnector(host, port)) {
            final JMSQueueControl queueControl = queueControlOf(connector, brokerName, destinationName);
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
    }

    @Override
    public long reprocess(final String host, final String port, final String brokerName, final String destinationName, final Iterator<String> msgIds) throws Exception {
        try (final JMXConnector connector = getJMXConnector(host, port)) {
            final JMSQueueControl queueControl = queueControlOf(connector, brokerName, destinationName);
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
    }

    @Override
    public String[] queueNames(final String host, final String port, final String brokerName) throws Exception {
        try (final JMXConnector connector = getJMXConnector(host, port)) {
            JMSServerControl serverControl = serverControlOf(connector, brokerName);
            return serverControl.getQueueNames();
        }
    }

    protected JMXConnector getJMXConnector(final String host, final String port) throws IOException {
        return connect(new JMXServiceURL(format(JMX_URL, host, port)), emptyMap());
    }

    protected JMSServerControl serverControlOf(final JMXConnector connector, final String brokerName) throws Exception {
        final ObjectName on = ObjectNameBuilder.create(getDefaultJmxDomain(), brokerName, true).getJMSServerObjectName();
        return newProxyInstance(connector.getMBeanServerConnection(), on, JMSServerControl.class, false);
    }

    protected JMSQueueControl queueControlOf(final JMXConnector connector, final String brokerName, final String destinationName) throws Exception {
        final ObjectName on = ObjectNameBuilder.create(getDefaultJmxDomain(), brokerName, true).getJMSQueueObjectName(destinationName);
        return newProxyInstance(connector.getMBeanServerConnection(), on, JMSQueueControl.class, false);
    }
}