package uk.gov.justice.artemis.manager.connector.jms;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.Session;

import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.jms.client.ActiveMQJMSConnectionFactory;

public class JmsProcessor {

    public <T> T process(ActiveMQJMSConnectionFactory factory,
                    final String destinationName,
                    final JmsManagementFunction<T> jmsManagementFunction) throws JMSException {

       final Queue queue = ActiveMQJMSClient.createQueue(destinationName);

       try (final QueueConnection queueConnection = factory.createQueueConnection();
            final QueueSession queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            final QueueBrowser queueBrowser = queueSession.createBrowser(queue)) {

           return jmsManagementFunction.apply(queueBrowser);
       }
    }
}