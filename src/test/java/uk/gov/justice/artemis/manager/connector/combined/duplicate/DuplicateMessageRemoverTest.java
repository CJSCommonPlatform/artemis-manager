package uk.gov.justice.artemis.manager.connector.combined.duplicate;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.artemis.manager.connector.combined.CombinedManagementFunctionException;
import uk.gov.justice.artemis.manager.connector.combined.duplicate.DuplicateMessageRemover;
import uk.gov.justice.artemis.manager.connector.combined.duplicate.DuplicateMessages;

import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueSender;

import org.apache.activemq.artemis.api.jms.management.JMSQueueControl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DuplicateMessageRemoverTest {

    private static final String JMS_MESSAGE_ID_FORMAT = "JMSMessageID = 'ID:%s'";

    @InjectMocks
    private DuplicateMessageRemover duplicateMessageRemover;

    @Test
    public void shouldRemoveDuplicateMessagesOnly() throws Exception {

        final QueueSender queueSender = mock(QueueSender.class);
        final JMSQueueControl jmsQueueControl = mock(JMSQueueControl.class);
        final Map<String, Message> duplicateMessages = new HashMap<>();

        final Message message_1 = mock(Message.class);
        final Message message_2 = mock(Message.class);

        final String messageId_1 = randomUUID().toString();
        final String messageId_2 = randomUUID().toString();

        duplicateMessages.put(messageId_1, message_1);
        duplicateMessages.put(messageId_2, message_2);

        duplicateMessageRemover.removeDuplicatesOnly(queueSender, jmsQueueControl, new DuplicateMessages(duplicateMessages, null));

        verify(jmsQueueControl).removeMessages(format(JMS_MESSAGE_ID_FORMAT, messageId_1));
        verify(jmsQueueControl).removeMessages(format(JMS_MESSAGE_ID_FORMAT, messageId_2));
        verify(queueSender).send(message_1);
        verify(queueSender).send(message_2);
    }

    @Test
    public void shouldThrowCombinedManagementFunctionExceptionIfExceptionIsThrown() throws Exception {

        final QueueSender queueSender = mock(QueueSender.class);
        final JMSQueueControl jmsQueueControl = mock(JMSQueueControl.class);
        final Exception exception = mock(Exception.class);
        final Map<String, Message> duplicateMessages = new HashMap<>();

        final Message message = mock(Message.class);
        final String messageId = randomUUID().toString();

        duplicateMessages.put(messageId, message);

        when(jmsQueueControl.removeMessages(format(JMS_MESSAGE_ID_FORMAT, messageId))).thenThrow(exception);

        try {
            duplicateMessageRemover.removeDuplicatesOnly(queueSender, jmsQueueControl, new DuplicateMessages(duplicateMessages, null));
            fail();
        } catch (CombinedManagementFunctionException e) {
            assertThat(e.getMessage(), is("Failed to remove duplicates for JMSMessageID: " + messageId));
            assertThat(e.getCause(), is(exception));
        }
    }

    @Test
    public void shouldThrowCombinedManagementFunctionExceptionIfJMSExceptionIsThrown() throws Exception {

        final QueueSender queueSender = mock(QueueSender.class);
        final JMSQueueControl jmsQueueControl = mock(JMSQueueControl.class);
        final Exception jmsException = mock(JMSException.class);
        final Map<String, Message> duplicateMessages = new HashMap<>();

        final Message message = mock(Message.class);
        final String messageId = randomUUID().toString();

        duplicateMessages.put(messageId, message);

        when(jmsQueueControl.removeMessages(format(JMS_MESSAGE_ID_FORMAT, messageId))).thenReturn(2);
        doThrow(jmsException).when(queueSender).send(message);

        try {
            duplicateMessageRemover.removeDuplicatesOnly(queueSender, jmsQueueControl, new DuplicateMessages(duplicateMessages, null));
            fail();
        } catch (CombinedManagementFunctionException e) {
            assertThat(e.getMessage(), is("Failed to add message back onto queue, all messages have been deleted for JMSMessageID: " + messageId));
            assertThat(e.getCause(), is(jmsException));
        }
    }
}