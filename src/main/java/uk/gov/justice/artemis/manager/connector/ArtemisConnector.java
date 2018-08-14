package uk.gov.justice.artemis.manager.connector;

import java.util.Iterator;
import java.util.List;

public interface ArtemisConnector {

    List<MessageData> messagesOf(final String host, final String port, final String brokerName, final String destinationName) throws Exception;

    long remove(final String host, final String port, final String brokerName, final String destinationName, final Iterator<String> msgIds) throws Exception;

    long reprocess(final String host, final String port, final String brokerName, final String destinationName, final Iterator<String> msgIds) throws Exception;

    String[] queueNames(final String host, final String port, final String brokerName) throws Exception;
}