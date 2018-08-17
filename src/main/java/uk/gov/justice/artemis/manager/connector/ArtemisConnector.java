package uk.gov.justice.artemis.manager.connector;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public interface ArtemisConnector {

    List<MessageData> messagesOf(final String host, final String port, final String brokerName, final String destinationName) throws Exception;

    long remove(final String host, final String port, final String brokerName, final String destinationName, final Iterator<String> msgIds) throws Exception;

    long reprocess(final String host, final String port, final String brokerName, final String destinationName, final Iterator<String> msgIds) throws Exception;

    String[] queueNames(final String host, final String port, final String brokerName) throws Exception;

    String[] topicNames(final String host, final String port, final String brokerName) throws Exception;

    Map<String, Long> queueMessageCount(final String host, final String port, final String brokerName, final String[] queueNames) throws Exception;

    Map<String, Long> topicMessageCount(final String host, final String port, final String brokerName, final String[] topicNames) throws Exception;
}