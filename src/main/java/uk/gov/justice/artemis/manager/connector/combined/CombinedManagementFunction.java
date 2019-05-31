package uk.gov.justice.artemis.manager.connector.combined;

import javax.jms.QueueBrowser;
import javax.jms.QueueSender;

import org.apache.activemq.artemis.api.jms.management.JMSQueueControl;

public interface CombinedManagementFunction<T> {

    T apply(final QueueBrowser queueBrowser, final QueueSender queueSender, final JMSQueueControl jmsQueueControl);
}
