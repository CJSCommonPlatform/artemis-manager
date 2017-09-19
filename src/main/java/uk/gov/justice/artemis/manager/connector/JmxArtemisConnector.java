package uk.gov.justice.artemis.manager.connector;

import uk.gov.justice.artemis.manager.connector.jmx.JmxManagement;
import uk.gov.justice.artemis.manager.connector.jmx.JmxProcessor;
import uk.gov.justice.output.ConsolePrinter;

import java.util.Iterator;
import java.util.List;

import org.apache.activemq.artemis.api.jms.management.JMSServerControl;

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
    public String[] topicNames(final String host, final String port, final String brokerName) throws Exception {
        return jmxProcessor.processServerControl(host, port, brokerName, JMSServerControl::getTopicNames);
    }
}
