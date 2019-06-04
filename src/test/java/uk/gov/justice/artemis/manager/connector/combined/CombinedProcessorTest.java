package uk.gov.justice.artemis.manager.connector.combined;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import javax.jms.JMSException;
import javax.management.remote.JMXServiceURL;

import org.apache.activemq.artemis.api.core.management.ObjectNameBuilder;
import org.apache.activemq.artemis.jms.client.ActiveMQJMSConnectionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CombinedProcessorTest {

    @InjectMocks
    private CombinedProcessor combinedProcessor;

    @Test
    @SuppressWarnings("unchecked")
    public void shouldThrowRuntimeException() throws Exception {

        final ActiveMQJMSConnectionFactory activeMQJMSConnectionFactory = mock(ActiveMQJMSConnectionFactory.class);
        final JMSException jmsException = new JMSException("mock failure");

        when(activeMQJMSConnectionFactory.createQueueConnection()).thenThrow(jmsException);

        try {
            combinedProcessor.process(
                    activeMQJMSConnectionFactory,
                    singletonList(mock(JMXServiceURL.class)),
                    new HashMap<String, String[]>(),
                    ObjectNameBuilder.create("domain"),
                    "destinationName",
                    mock(CombinedManagementFunction.class)
            );
            fail();
        } catch (final CombinedProcessorFailureException exception) {
            assertThat(exception.getMessage(), is("Error connecting to queue to apply JMS management function"));
            assertThat(exception.getCause(), is(jmsException));
        }
    }
}