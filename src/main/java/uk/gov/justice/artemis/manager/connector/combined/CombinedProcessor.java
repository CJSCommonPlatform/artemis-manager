package uk.gov.justice.artemis.manager.connector.combined;

import static javax.management.MBeanServerInvocationHandler.newProxyInstance;
import static javax.management.remote.JMXConnectorFactory.connect;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;

import org.apache.activemq.artemis.api.core.management.ObjectNameBuilder;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.api.jms.management.JMSQueueControl;
import org.apache.activemq.artemis.jms.client.ActiveMQJMSConnectionFactory;

public class CombinedProcessor {

    public <T> T processQueueSender(final List<JMXServiceURL> serviceUrls,
                                    final Map<String, ?> environment,
                                    final ObjectNameBuilder objectNameBuilder,
                                    final ActiveMQJMSConnectionFactory activeMQJMSConnectionFactory,
                                    final String destinationName,
                                    final CombinedFunction<T> combinedFunction) {

        final Queue queue = ActiveMQJMSClient.createQueue(destinationName);

        try (final JMXConnector connector = getJMXConnector(serviceUrls.get(0), environment);
             final QueueConnection queueConnection = activeMQJMSConnectionFactory.createQueueConnection();
             final QueueSession queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
             final QueueBrowser queueBrowser = queueSession.createBrowser(queue);
             final QueueSender queueSender = queueSession.createSender(queue)) {

            final JMSQueueControl queueControl = queueControlOf(connector, objectNameBuilder, destinationName);

            return combinedFunction.apply(queueSession, queueBrowser, queueSender, queueControl);

        } catch (final Exception e) {
            throw new CombinedProcessorFailureException("Error connecting to queue to apply JMS management or JMX management functions", e);
        }
    }

    private JMXConnector getJMXConnector(final JMXServiceURL jmxServiceUrl, final Map<String, ?> environment) throws IOException {
        return connect(jmxServiceUrl, environment);
    }

    private JMSQueueControl queueControlOf(final JMXConnector jmxConnector,
                                           final ObjectNameBuilder objectNameBuilder,
                                           final String destinationName) throws Exception {
        final ObjectName objectName = objectNameBuilder.getJMSQueueObjectName(destinationName);
        return newProxyInstance(jmxConnector.getMBeanServerConnection(), objectName, JMSQueueControl.class, false);
    }
}
