package uk.gov.justice.artemis.manager.connector.jmx;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;
import static javax.management.MBeanServerInvocationHandler.newProxyInstance;
import static javax.management.remote.JMXConnectorFactory.connect;
import static org.apache.activemq.artemis.api.config.ActiveMQDefaultConfiguration.getDefaultJmxDomain;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;

import org.apache.activemq.artemis.api.core.management.ObjectNameBuilder;
import org.apache.activemq.artemis.api.jms.management.JMSQueueControl;
import org.apache.activemq.artemis.api.jms.management.JMSServerControl;
import org.apache.activemq.artemis.api.jms.management.TopicControl;

public class JmxProcessor {

    private static final String JMX_URL = "service:jmx:rmi:///jndi/rmi://%s:%s/jmxrmi";

    public <T> T processQueueControl(final String host,
                                     final String port,
                                     final String brokerName,
                                     final String destinationName,
                                     final JmxManagementFunction<T> jmxManagementFunction) throws Exception {

        try (final JMXConnector connector = getJMXConnector(host, port)) {
            final JMSQueueControl queueControl = queueControlOf(connector, brokerName, destinationName);

            return jmxManagementFunction.apply(queueControl);
        }
    }

    public <T> T processServerControl(final String host,
                                      final String port,
                                      final String brokerName,
                                      final Function<JMSServerControl, T> fn) throws Exception {

        try (final JMXConnector connector = getJMXConnector(host, port)) {
            final JMSServerControl serverControl = serverControlOf(connector, brokerName);

            return fn.apply(serverControl);
        }
    }

    public <T> Map<String, T> processQueues(final String host,
                                            final String port,
                                            final String brokerName,
                                            final String[] destinations,
                                            final Function<JMSQueueControl, T> fn) throws Exception {
        try (final JMXConnector connector = getJMXConnector(host, port)) {
            return Arrays.stream(destinations).collect(toMap(Function.identity(), destination -> {
                try {
                    return processQueueControl(connector, brokerName, destination, fn);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }
    }

    public <T> Map<String, T> processTopics(final String host,
                                            final String port,
                                            final String brokerName,
                                            final String[] destinations,
                                            final Function<TopicControl, T> fn) throws Exception {
        try (final JMXConnector connector = getJMXConnector(host, port)) {
            return Arrays.stream(destinations).collect(toMap(Function.identity(), destination -> {
                try {
                    return processTopicControl(connector, brokerName, destination, fn);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }
    }

    private JMXConnector getJMXConnector(final String host, final String port) throws IOException {
        return connect(new JMXServiceURL(format(JMX_URL, host, port)), emptyMap());
    }

    private JMSQueueControl queueControlOf(final JMXConnector connector, final String brokerName, final String destinationName) throws Exception {
        final ObjectName on = ObjectNameBuilder.create(getDefaultJmxDomain(), brokerName, true).getJMSQueueObjectName(destinationName);
        return newProxyInstance(connector.getMBeanServerConnection(), on, JMSQueueControl.class, false);
    }

    private TopicControl topicControlOf(final JMXConnector connector, final String brokerName, final String destinationName) throws Exception {
        final ObjectName on = ObjectNameBuilder.create(getDefaultJmxDomain(), brokerName, true).getJMSTopicObjectName(destinationName);
        return newProxyInstance(connector.getMBeanServerConnection(), on, TopicControl.class, false);
    }

    private JMSServerControl serverControlOf(final JMXConnector connector, final String brokerName) throws Exception {
        final ObjectName on = ObjectNameBuilder.create(getDefaultJmxDomain(), brokerName, true).getJMSServerObjectName();
        return newProxyInstance(connector.getMBeanServerConnection(), on, JMSServerControl.class, false);
    }

    private <T> T processQueueControl(final JMXConnector connector,
                                      final String brokerName,
                                      final String destination,
                                      final Function<JMSQueueControl, T> fn) throws Exception {
        final JMSQueueControl queueControl = queueControlOf(connector, brokerName, destination);
        return fn.apply(queueControl);
    }

    private <T> T processTopicControl(final JMXConnector connector,
                                      final String brokerName,
                                      final String destination,
                                      final Function<TopicControl, T> fn) throws Exception {
        final TopicControl topicControl = topicControlOf(connector, brokerName, destination);
        return fn.apply(topicControl);
    }
}