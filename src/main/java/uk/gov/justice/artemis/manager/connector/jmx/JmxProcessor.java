package uk.gov.justice.artemis.manager.connector.jmx;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static javax.management.MBeanServerInvocationHandler.newProxyInstance;
import static javax.management.remote.JMXConnectorFactory.connect;
import static org.apache.activemq.artemis.api.config.ActiveMQDefaultConfiguration.getDefaultJmxDomain;

import java.io.IOException;

import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;

import org.apache.activemq.artemis.api.core.management.ObjectNameBuilder;
import org.apache.activemq.artemis.api.jms.management.JMSQueueControl;
import org.apache.activemq.artemis.api.jms.management.JMSServerControl;

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
                                      final JmxServerControlFunction<T> jmxServerControlFunction) throws Exception {

        try (final JMXConnector connector = getJMXConnector(host, port)) {
            final JMSServerControl serverControl = serverControlOf(connector, brokerName);

            return jmxServerControlFunction.apply(serverControl);
        }
    }

    private JMXConnector getJMXConnector(final String host, final String port) throws IOException {
        return connect(new JMXServiceURL(format(JMX_URL, host, port)), emptyMap());
    }

    private JMSQueueControl queueControlOf(final JMXConnector connector, final String brokerName, final String destinationName) throws Exception {
        final ObjectName on = ObjectNameBuilder.create(getDefaultJmxDomain(), brokerName, true).getJMSQueueObjectName(destinationName);
        return newProxyInstance(connector.getMBeanServerConnection(), on, JMSQueueControl.class, false);
    }

    private JMSServerControl serverControlOf(final JMXConnector connector, final String brokerName) throws Exception {
        final ObjectName on = ObjectNameBuilder.create(getDefaultJmxDomain(), brokerName, true).getJMSServerObjectName();
        return newProxyInstance(connector.getMBeanServerConnection(), on, JMSServerControl.class, false);
    }
}
