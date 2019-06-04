package uk.gov.justice.artemis.manager.connector.combined;

import static javax.jms.Session.AUTO_ACKNOWLEDGE;
import static javax.management.MBeanServerInvocationHandler.newProxyInstance;

import java.util.List;
import java.util.Map;

import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.activemq.artemis.api.core.management.ObjectNameBuilder;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.api.jms.management.JMSQueueControl;
import org.apache.activemq.artemis.jms.client.ActiveMQJMSConnectionFactory;

public class CombinedProcessor {

    public <T> T process(final ActiveMQJMSConnectionFactory factory,
                         final List<JMXServiceURL> jmxServiceUrls,
                         final Map<String, String[]> jmxEnvironment,
                         final ObjectNameBuilder objectNameBuilder,
                         final String destinationName,
                         final CombinedManagementFunction<T> jmsManagementFunction) {

        final Queue queue = ActiveMQJMSClient.createQueue(destinationName);

        try (final QueueConnection queueConnection = factory.createQueueConnection();
             final QueueSession queueSession = queueConnection.createQueueSession(false, AUTO_ACKNOWLEDGE);
             final QueueBrowser queueBrowser = queueSession.createBrowser(queue);
             final QueueSender queueSender = queueSession.createSender(queue);
             final JMXConnector jmxConnector = JMXConnectorFactory.connect(jmxServiceUrls.get(0), jmxEnvironment)) {

            return jmsManagementFunction.apply(queueBrowser, queueSender, queueControlOf(jmxConnector, objectNameBuilder, destinationName));

        } catch (final Exception e) {
            throw new CombinedProcessorFailureException("Error connecting to queue to apply JMS management function", e);
        }
    }

    private JMSQueueControl queueControlOf(final JMXConnector connector,
                                           final ObjectNameBuilder objectNameBuilder,
                                           final String destinationName) {

        try {

            final ObjectName objectName = objectNameBuilder.getJMSQueueObjectName(destinationName);
            return newProxyInstance(connector.getMBeanServerConnection(), objectName, JMSQueueControl.class, false);

        } catch (final Exception e) {
            throw new CombinedProcessorFailureException("Error creating JMSQueueControl for queue", e);
        }
    }
}
