package uk.gov.justice.artemis.manager.connector;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static javax.management.MBeanServerInvocationHandler.newProxyInstance;
import static javax.management.remote.JMXConnectorFactory.connect;
import static org.apache.activemq.artemis.api.config.ActiveMQDefaultConfiguration.getDefaultJmxDomain;

import java.util.HashMap;
import java.util.List;

import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;

import org.apache.activemq.artemis.api.core.management.ObjectNameBuilder;
import org.apache.activemq.artemis.api.jms.management.JMSQueueControl;

public class JmxArtemisConnector implements ArtemisConnector {
    private static final String JMX_URL = "service:jmx:rmi:///jndi/rmi://%s:%s/jmxrmi";
    public static final String JMS_MESSAGE_ID = "JMSMessageID";
    public static final String ORIGINAL_DESTINATION = "OriginalDestination";
    public static final String TEXT = "Text";

    @Override
    public List<MessageData> messagesOf(final String host, final String port, final String brokerName, final String destinationName) throws Exception {
        final CompositeData[] browseResult = queueControlOf(host, port, brokerName, destinationName).browse();
        return stream(browseResult)
                .map(cd -> new MessageData(String.valueOf(cd.get(JMS_MESSAGE_ID)), String.valueOf(cd.get(ORIGINAL_DESTINATION)), String.valueOf(cd.get(TEXT))))
                .collect(toList());

    }

    private JMSQueueControl queueControlOf(final String host, final String port, final String brokerName, final String destinationName) throws Exception {
        final ObjectName on = ObjectNameBuilder.create(getDefaultJmxDomain(), brokerName, true).getJMSQueueObjectName(destinationName);
        final JMXConnector connector = connect(new JMXServiceURL(format(JMX_URL, host, port)), new HashMap());
        return newProxyInstance(connector.getMBeanServerConnection(), on, JMSQueueControl.class, false);
    }
}
