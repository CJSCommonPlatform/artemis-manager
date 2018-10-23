package uk.gov.justice.artemis.manager.connector.combined;

import javax.jms.QueueBrowser;
import javax.jms.QueueSender;
import javax.jms.QueueSession;

import org.apache.activemq.artemis.api.jms.management.JMSQueueControl;

@FunctionalInterface
public interface CombinedFunction<T> {

    T apply(final QueueSession queueSession,
            final QueueBrowser queueBrowser,
            final QueueSender queueSender,
            final JMSQueueControl queueControl);
}
