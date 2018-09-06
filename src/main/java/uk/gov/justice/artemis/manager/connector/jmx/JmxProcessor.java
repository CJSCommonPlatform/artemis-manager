package uk.gov.justice.artemis.manager.connector.jmx;

import static java.util.stream.Collectors.toMap;
import static javax.management.MBeanServerInvocationHandler.newProxyInstance;
import static javax.management.remote.JMXConnectorFactory.connect;
import static org.apache.activemq.artemis.api.config.ActiveMQDefaultConfiguration.getDefaultJmxDomain;
import static pl.touk.throwing.ThrowingFunction.unchecked;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;

import org.apache.activemq.artemis.api.core.management.ObjectNameBuilder;
import org.apache.activemq.artemis.api.jms.management.JMSQueueControl;
import org.apache.activemq.artemis.api.jms.management.JMSServerControl;
import org.apache.activemq.artemis.api.jms.management.TopicControl;

public class JmxProcessor {

    public List<JMXServiceURL> processJmxUrls(final Collection<String> jmxUrls) {
        return jmxUrls.stream().map(unchecked(JMXServiceURL::new)).collect(Collectors.toList());
    }

    public ObjectNameBuilder getObjectNameBuilder(String brokerName) {
        return ObjectNameBuilder.create(getDefaultJmxDomain(), brokerName, true);
    }

    public <T> T processQueueControl(
                    final JMXConnector connector,
                    final ObjectNameBuilder onb,
                    final String destinationName,
                    final JmxManagementFunction<T> jmxManagementFunction) throws Exception {

        final JMSQueueControl queueControl = queueControlOf(connector, onb, destinationName);
        return jmxManagementFunction.apply(queueControl);
    }

    public <T> Stream<T> processQueueControl(
                    final List<JMXServiceURL> serviceUrls,
                    final Map<String,?> env,
                    final ObjectNameBuilder onb,
                    final String destinationName,
                    final JmxManagementFunction<T> jmxManagementFunction) {

        return serviceUrls.stream().map(s -> {
            try (final JMXConnector connector = getJMXConnector(s, env)) {
                return processQueueControl(connector, onb, destinationName, jmxManagementFunction);
            } catch (Exception e) {
                throw new JmxProcessorFailureException("Error while processing queue control", e);
            }});
    }

    public <T> T processServerControl(final JMXConnector connector,
                    final ObjectNameBuilder onb,
                    final Function<JMSServerControl, T> fn) throws Exception {

        final JMSServerControl serverControl = serverControlOf(connector, onb);
        return fn.apply(serverControl);
    }

    public <T> Stream<T> processServerControl(final List<JMXServiceURL> serviceUrls,
                    final Map<String, ?> env,
                    final ObjectNameBuilder onb,
                    final Function<JMSServerControl, T> fn) {

        return serviceUrls.stream().map(s -> {
            try (final JMXConnector connector = getJMXConnector(s, env)) {
                return processServerControl(connector, onb, fn);
            } catch (Exception e) {
                throw new JmxProcessorFailureException("Error while processing server control", e);
            }});
    }

    public <T> Map<String, T> processQueues(final JMXConnector connector,
                    final ObjectNameBuilder onb,
                    final Collection<String> destinations,
                    final Function<JMSQueueControl, T> fn) {
        return destinations.stream().collect(toMap(Function.identity(), destination -> {
            try {
                return processQueueControl(connector, onb, destination, fn);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            }));
    }

    public <T> Stream<Map<String, T>> processQueues(final List<JMXServiceURL> serviceUrls,
                    final Map<String, ?> env,
                    final ObjectNameBuilder onb,
                    final Collection<String> destinations,
                    final Function<JMSQueueControl, T> fn) {
        return serviceUrls.stream().map(s -> {
            try (final JMXConnector connector = getJMXConnector(s, env)) {
                return processQueues(connector, onb, destinations, fn);
            } catch (Exception e) {
                throw new JmxProcessorFailureException("Error while processing queues", e);
            }});
    }

    public <T> Map<String, T> processTopics(JMXConnector connector,
                            final ObjectNameBuilder onb,
                            final Collection<String> destinations,
                            final Function<TopicControl, T> fn) {
        return destinations.stream().collect(toMap(Function.identity(), destination -> {
            try {
                return processTopicControl(connector, onb, destination, fn);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));
    }

    public <T> Stream<Map<String, T>> processTopics(final List<JMXServiceURL> serviceUrls,
                    final Map<String, ?> env,
                    final ObjectNameBuilder onb,
                    final Collection<String> destinations,
                    final Function<TopicControl, T> fn) {
        return serviceUrls.stream().map(s -> {
            try (final JMXConnector connector = getJMXConnector(s, env)) {
                return processTopics(connector, onb, destinations, fn);
            } catch (Exception e) {
                throw new JmxProcessorFailureException("Error while processing topics", e);
            }});
    }

    private JMXConnector getJMXConnector(final JMXServiceURL jmxServiceUrl, final Map<String, ?> env) throws IOException {
        return connect(jmxServiceUrl, env);
    }

    private JMSQueueControl queueControlOf(final JMXConnector connector, final ObjectNameBuilder onb, final String destinationName) throws Exception {
        final ObjectName on = onb.getJMSQueueObjectName(destinationName);
        return newProxyInstance(connector.getMBeanServerConnection(), on, JMSQueueControl.class, false);
    }

    private TopicControl topicControlOf(final JMXConnector connector, final ObjectNameBuilder onb, final String destinationName) throws Exception {
        final ObjectName on = onb.getJMSTopicObjectName(destinationName);
        return newProxyInstance(connector.getMBeanServerConnection(), on, TopicControl.class, false);
    }

    private JMSServerControl serverControlOf(final JMXConnector connector, final ObjectNameBuilder onb) throws Exception {
        final ObjectName on = onb.getJMSServerObjectName();
        return newProxyInstance(connector.getMBeanServerConnection(), on, JMSServerControl.class, false);
    }

    private <T> T processQueueControl(final JMXConnector connector,
                    final ObjectNameBuilder onb,
                    final String destination,
                    final Function<JMSQueueControl, T> fn) throws Exception {
        final JMSQueueControl queueControl = queueControlOf(connector, onb, destination);
        return fn.apply(queueControl);
    }

    private <T> T processTopicControl(final JMXConnector connector,
                                      final ObjectNameBuilder onb,
                                      final String destination,
                                      final Function<TopicControl, T> fn) throws Exception {
        final TopicControl topicControl = topicControlOf(connector, onb, destination);
        return fn.apply(topicControl);
    }
}