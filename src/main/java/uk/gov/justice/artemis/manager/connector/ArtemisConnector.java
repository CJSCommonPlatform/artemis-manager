package uk.gov.justice.artemis.manager.connector;

import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public interface ArtemisConnector {

    List<MessageData> messagesOf(final String destinationName) throws Exception;

    long remove(final String destinationName, final Iterator<String> msgIds) throws Exception;

    long reprocess(final String destinationName, final Iterator<String> msgIds) throws Exception;

    List<String> queueNames() throws Exception;

    List<String> topicNames() throws Exception;

    Map<String, Long> queueMessageCount(final Collection<String> queueNames) throws Exception;

    Map<String, Long> topicMessageCount(final Collection<String> queueNames) throws Exception;

    void setParameters(final List<String> jmxUrls,
                    final String brokerName,
                    final String jmxUsername,
                    final String jmxPassword,
                    final String jmsUrl,
                    final String jmsUsername,
                    final String jmsPassword) throws MalformedURLException;
}