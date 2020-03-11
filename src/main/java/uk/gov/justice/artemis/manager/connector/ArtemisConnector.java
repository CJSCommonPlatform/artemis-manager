package uk.gov.justice.artemis.manager.connector;

import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public interface ArtemisConnector {

    List<MessageData> messagesOf(final String destinationName);

    long remove(final String destinationName, final Iterator<String> msgIds);

    List<String> removeAllDuplicates(final String destinationName);

    List<String> deduplicateTopicMessages(final String destinationName);

    long reprocess(final String destinationName, final Iterator<String> msgIds);

    int reprocessAll(final String destinationName);

    List<String> queueNames();

    List<String> topicNames();

    Map<String, Long> queueMessageCount(final Collection<String> queueNames);

    Map<String, Long> topicMessageCount(final Collection<String> queueNames);

    void setParameters(final List<String> jmxUrls,
                       final String brokerName,
                       final String jmxUsername,
                       final String jmxPassword,
                       final String jmsUrl,
                       final String jmsUsername,
                       final String jmsPassword) throws MalformedURLException;

    String sendTextMessage(final String destinationName, final String message);
}
