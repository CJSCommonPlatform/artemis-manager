package uk.gov.justice.artemis.manager.connector.jms;

import static java.lang.String.format;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.Session;

import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.jms.client.ActiveMQQueueConnectionFactory;

public class JmsProcessor {

    private static final String JMS_URL = "tcp://%s:%s";

    public <T> T process(final String host,
                         final String port,
                         final String destinationName,
                         final JmsManagementFunction<T> jmsManagementFunction) throws JMSException {

        final Queue queue = ActiveMQJMSClient.createQueue(destinationName);

        try (final ActiveMQQueueConnectionFactory connectionFactory = new ActiveMQQueueConnectionFactory(format(JMS_URL, host, port));
             final QueueConnection queueConnection = connectionFactory.createQueueConnection();
             final QueueSession queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
             final QueueBrowser queueBrowser = queueSession.createBrowser(queue)) {

            return jmsManagementFunction.apply(queueBrowser);
        }
    }
}
