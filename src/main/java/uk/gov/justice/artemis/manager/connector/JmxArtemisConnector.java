package uk.gov.justice.artemis.manager.connector;

import static pl.touk.throwing.ThrowingFunction.unchecked;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.activemq.artemis.api.jms.management.DestinationControl;
import org.apache.activemq.artemis.api.jms.management.JMSServerControl;

import uk.gov.justice.artemis.manager.connector.jmx.JmxManagement;
import uk.gov.justice.artemis.manager.connector.jmx.JmxProcessor;
import uk.gov.justice.output.ConsolePrinter;

public class JmxArtemisConnector implements ArtemisConnector {

    private final JmxProcessor jmxProcessor = new JmxProcessor();
    private final JmxManagement jmxManagement = new JmxManagement(new ConsolePrinter());

    @Override
    public List<MessageData> messagesOf(final String host, final String port, final String brokerName, final String destinationName) throws Exception {
        return jmxProcessor.processQueueControl(host, port, brokerName, destinationName, jmxManagement.browseMessages());
    }

    @Override
    public long remove(final String host, final String port, final String brokerName, final String destinationName, final Iterator<String> msgIds) throws Exception {
        return jmxProcessor.processQueueControl(host, port, brokerName, destinationName, jmxManagement.removeMessages(msgIds));
    }

    @Override
    public long reprocess(final String host, final String port, final String brokerName, final String destinationName, final Iterator<String> msgIds) throws Exception {
        return jmxProcessor.processQueueControl(host, port, brokerName, destinationName, jmxManagement.reprocessMessages(msgIds));
    }

    @Override
    public String[] queueNames(final String host, final String port, final String brokerName) throws Exception {
        return jmxProcessor.processServerControl(host, port, brokerName, JMSServerControl::getQueueNames);
    }

    @Override
    public Map<String, Long> queueMessageCount(final String host, final String port, final String brokerName, final String[] queueNames) throws Exception {
        return jmxProcessor.processQueues(host, port, brokerName, queueNames, unchecked(DestinationControl::getMessageCount));
    }

    @Override
    public String[] topicNames(final String host, final String port, final String brokerName) throws Exception {
        return jmxProcessor.processServerControl(host, port, brokerName, JMSServerControl::getTopicNames);
    }

    @Override
    public Map<String, Long> topicMessageCount(final String host, final String port, final String brokerName, final String[] topicNames) throws Exception {
        return jmxProcessor.processTopics(host, port, brokerName, topicNames, unchecked(DestinationControl::getMessageCount));
    }
}
