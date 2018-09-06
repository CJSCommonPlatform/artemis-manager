package uk.gov.justice.artemis.manager.connector;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingLong;
import static java.util.stream.Collectors.toList;
import static pl.touk.throwing.ThrowingFunction.unchecked;

import uk.gov.justice.artemis.manager.connector.jms.JmsManagement;
import uk.gov.justice.artemis.manager.connector.jms.JmsProcessor;
import uk.gov.justice.artemis.manager.connector.jmx.JmxManagement;
import uk.gov.justice.artemis.manager.connector.jmx.JmxProcessor;
import uk.gov.justice.output.ConsolePrinter;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;

import org.apache.activemq.artemis.api.core.management.ObjectNameBuilder;
import org.apache.activemq.artemis.api.jms.management.DestinationControl;
import org.apache.activemq.artemis.api.jms.management.JMSServerControl;
import org.apache.activemq.artemis.jms.client.ActiveMQJMSConnectionFactory;

/**
 * reprocess, remove and messagesOf were re-implemented in JMS due to issues with large messages
 * over JMX.
 */
public class CombinedJmsAndJmxArtemisConnector implements ArtemisConnector {

    private final JmxProcessor jmxProcessor = new JmxProcessor();
    private final JmxManagement jmxManagement = new JmxManagement(new ConsolePrinter());
    private final JmsProcessor jmsProcessor = new JmsProcessor();
    private final JmsManagement jmsManagement = new JmsManagement();

    private List<JMXServiceURL> jmxServiceUrls;
    private Map<String,String[]> jmxEnvironment;
    private ObjectNameBuilder objectNameBuilder;

    private ActiveMQJMSConnectionFactory jmsFactory;

    @Override
    public void setParameters(final List<String> jmxUrls,
                    final String brokerName,
                    final String jmxUsername,
                    final String jmxPassword,
                    final String jmsUrl,
                    final String jmsUsername,
                    final String jmsPassword) {
        this.jmxServiceUrls = jmxProcessor.processJmxUrls(jmxUrls);
        this.objectNameBuilder = jmxProcessor.getObjectNameBuilder(brokerName);

        if ((jmxUsername != null) && (jmxPassword != null)) {
            this.jmxEnvironment = new HashMap<>();
            this.jmxEnvironment.put(JMXConnector.CREDENTIALS, new String[]{ jmxUsername, jmxPassword });
        } else {
            this.jmxEnvironment = emptyMap();
        }

        if ((jmsUsername != null) && (jmsPassword != null)) {
            this.jmsFactory = new ActiveMQJMSConnectionFactory(jmsUrl, jmsUsername, jmsPassword);
        } else {
            this.jmsFactory = new ActiveMQJMSConnectionFactory(jmsUrl);
        }
    }

    @Override
    public List<MessageData> messagesOf(final String destinationName) {
        return jmsProcessor.process(this.jmsFactory, destinationName, jmsManagement.browseMessages());
    }

    @Override
    public long remove(final String destinationName, final Iterator<String> msgIds) {
        return jmxProcessor.processQueueControl(this.jmxServiceUrls,
                this.jmxEnvironment,
                this.objectNameBuilder,
                destinationName,
                jmxManagement.removeMessages(msgIds)).mapToLong(Long::longValue).sum();
    }

    @Override
    public long reprocess(final String destinationName, final Iterator<String> msgIds) {
        return jmxProcessor.processQueueControl(
                this.jmxServiceUrls,
                this.jmxEnvironment,
                this.objectNameBuilder,
                destinationName,
                jmxManagement.reprocessMessages(msgIds)).mapToLong(Long::longValue).sum();
    }

    @Override
    public List<String> queueNames() {
        return jmxProcessor.processServerControl(
            this.jmxServiceUrls,
            this.jmxEnvironment,
            this.objectNameBuilder,
            JMSServerControl::getQueueNames).flatMap(
                Arrays::stream).sorted().
                distinct().collect(toList());
    }


    @Override
    public Map<String, Long> queueMessageCount(final Collection<String> queueNames) {
        return jmxProcessor.processQueues(this.jmxServiceUrls,
            this.jmxEnvironment,
            this.objectNameBuilder, 
            queueNames,
            unchecked(DestinationControl::getMessageCount)).flatMap(
                m -> m.entrySet().stream()).collect(
                    groupingBy(Entry::getKey,
                    summingLong(Entry::getValue)));
    }

    @Override
    public List<String> topicNames() {
        return jmxProcessor.processServerControl(this.jmxServiceUrls,
            this.jmxEnvironment,
            this.objectNameBuilder,
            JMSServerControl::getTopicNames).flatMap(
                Arrays::stream).sorted().
                    distinct().collect(toList());
    }

    @Override
    public Map<String, Long> topicMessageCount(final Collection<String> topicNames) {
        return jmxProcessor.processTopics(this.jmxServiceUrls,
            this.jmxEnvironment,
            this.objectNameBuilder, 
            topicNames,
            unchecked(DestinationControl::getMessageCount)).flatMap(
                m -> m.entrySet().stream()).collect(
                    groupingBy(Entry::getKey,
                    summingLong(Entry::getValue)));
    }
}