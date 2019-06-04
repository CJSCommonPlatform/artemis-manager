package uk.gov.justice.artemis.manager.connector.jms;

import static javax.jms.Session.AUTO_ACKNOWLEDGE;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;

import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.jms.client.ActiveMQJMSConnectionFactory;

public class JmsProcessor {

    public <T> T process(final ActiveMQJMSConnectionFactory factory,
                         final String destinationName,
                         final JmsManagementFunction<T> jmsManagementFunction) {

        final Queue queue = ActiveMQJMSClient.createQueue(destinationName);

        try (final QueueConnection queueConnection = factory.createQueueConnection();
             final QueueSession queueSession = queueConnection.createQueueSession(false, AUTO_ACKNOWLEDGE);
             final QueueBrowser queueBrowser = queueSession.createBrowser(queue)) {

            return jmsManagementFunction.apply(queueBrowser);
        } catch (final JMSException e) {
            throw new JmsProcessorFailureException("Error connecting to queue to apply JMS management function", e);
        }
    }
}