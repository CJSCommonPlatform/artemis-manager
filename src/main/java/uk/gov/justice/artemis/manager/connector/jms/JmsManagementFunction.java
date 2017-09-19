package uk.gov.justice.artemis.manager.connector.jms;

import javax.jms.QueueBrowser;

@FunctionalInterface
public interface JmsManagementFunction<T> {

    T apply(final QueueBrowser queueBrowser);
}
