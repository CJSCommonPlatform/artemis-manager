package uk.gov.justice.artemis.manager.connector.combined.duplicate;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.jms.Message;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JmsMessageUtilTest {

    @InjectMocks
    private JmsMessageUtil jmsMessageUtil;

    @Test
    public void shouldReturnJmsMessageIdFromMessage() throws Exception {

        final String messageId = randomUUID().toString();
        final Message message = mock(Message.class);

        when(message.getJMSMessageID()).thenReturn("ID:" + messageId);

        assertThat(jmsMessageUtil.getJmsMessageIdFrom(message), is(messageId));
    }

    @Test
    public void shouldReturnConsumerFromMessage() throws Exception {

        final String consumer = "consumer";
        final Message message = mock(Message.class);

        when(message.getStringProperty("_AMQ_ORIG_QUEUE")).thenReturn(consumer);

        assertThat(jmsMessageUtil.getConsumerFrom(message), is(consumer));
    }
}