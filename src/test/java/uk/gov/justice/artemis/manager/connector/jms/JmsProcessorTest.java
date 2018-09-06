package uk.gov.justice.artemis.manager.connector.jms;

import static org.mockito.Mockito.when;

import javax.jms.JMSException;

import org.apache.activemq.artemis.jms.client.ActiveMQJMSConnectionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JmsProcessorTest {

    @Mock
    private ActiveMQJMSConnectionFactory factory;

    @Mock
    private JmsManagementFunction<Object> function;

    @InjectMocks
    private JmsProcessor jmsProcessor;

    @Test(expected = JmsProcessorFailureException.class)
    public void shouldThrowRuntimeException() throws Exception {

        when(factory.createQueueConnection()).thenThrow(new JMSException("mock failure"));

        jmsProcessor.process(factory, "DummyDestination", function);
    }
}