package uk.gov.justice.artemis.manager.connector.jmx;

import org.apache.activemq.artemis.api.jms.management.JMSQueueControl;

@FunctionalInterface
public interface JmxManagementFunction<T> {

    T apply(final JMSQueueControl queueControl);
}
