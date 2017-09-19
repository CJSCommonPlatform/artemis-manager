package uk.gov.justice.artemis.manager.connector.jmx;

import org.apache.activemq.artemis.api.jms.management.JMSServerControl;

@FunctionalInterface
public interface JmxServerControlFunction<T> {

    T apply(final JMSServerControl jmsServerControl);
}
