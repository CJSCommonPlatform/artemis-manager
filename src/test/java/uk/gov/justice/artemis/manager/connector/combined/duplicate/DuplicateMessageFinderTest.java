package uk.gov.justice.artemis.manager.connector.combined.duplicate;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyEnumeration;
import static java.util.Collections.enumeration;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.artemis.manager.connector.combined.CombinedManagementFunctionException;
import uk.gov.justice.artemis.manager.connector.combined.duplicate.DuplicateMessageFinder;
import uk.gov.justice.artemis.manager.connector.combined.duplicate.DuplicateMessages;
import uk.gov.justice.artemis.manager.connector.combined.duplicate.JmsMessageUtil;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueBrowser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DuplicateMessageFinderTest {

    @Mock
    private JmsMessageUtil jmsMessageUtil;

    @InjectMocks
    private DuplicateMessageFinder duplicateMessageFinder;

    @Test
    public void shouldReturnSingleInstanceOfDuplicatedMessages() throws Exception {

        final QueueBrowser queueBrowser = mock(QueueBrowser.class);
        final Message message_1 = mock(Message.class);
        final Message message_2 = mock(Message.class);
        final Message message_3 = mock(Message.class);
        final Message message_4 = mock(Message.class);
        final Message message_5 = mock(Message.class);
        final Message message_6 = mock(Message.class);

        final String jmsMessageId_1 = randomUUID().toString();
        final String jmsMessageId_2 = randomUUID().toString();
        final String jmsMessageId_3 = randomUUID().toString();

        when(jmsMessageUtil.getJmsMessageIdFrom(message_1)).thenReturn(jmsMessageId_1);
        when(jmsMessageUtil.getConsumerFrom(message_1)).thenReturn("artemis-manager.command_handler");
        when(jmsMessageUtil.getJmsMessageIdFrom(message_2)).thenReturn(jmsMessageId_1);
        when(jmsMessageUtil.getConsumerFrom(message_2)).thenReturn("artemis-manager.command_handler");
        when(jmsMessageUtil.getJmsMessageIdFrom(message_3)).thenReturn(jmsMessageId_1);
        when(jmsMessageUtil.getConsumerFrom(message_3)).thenReturn("artemis-manager.command_handler");
        when(jmsMessageUtil.getJmsMessageIdFrom(message_4)).thenReturn(jmsMessageId_2);
        when(jmsMessageUtil.getConsumerFrom(message_4)).thenReturn("artemis-manager.command_handler");
        when(jmsMessageUtil.getJmsMessageIdFrom(message_5)).thenReturn(jmsMessageId_3);
        when(jmsMessageUtil.getConsumerFrom(message_5)).thenReturn("artemis-manager.command_handler");
        when(jmsMessageUtil.getJmsMessageIdFrom(message_6)).thenReturn(jmsMessageId_3);
        when(jmsMessageUtil.getConsumerFrom(message_6)).thenReturn("artemis-manager.command_handler");

        final Enumeration<Message> messageEnumeration = enumeration(asList(message_1, message_2, message_3, message_4, message_5, message_6));

        when(queueBrowser.getEnumeration()).thenReturn(messageEnumeration);

        final DuplicateMessages duplicateMessages = duplicateMessageFinder.findDuplicateMessages(queueBrowser);

        final Map<String, Message> duplicateMessagesMap = duplicateMessages.getDuplicateMessages();
        assertThat(duplicateMessagesMap.size(), is(2));

        assertThat(duplicateMessages.getMessageCache().size(), is(3));

        final Collection<Message> values = duplicateMessagesMap.values();
        assertThat(values, hasItems(message_1, message_5));
    }

    @Test
    public void shouldNotReturnTopicDuplicateMessages() throws Exception {

        final QueueBrowser queueBrowser = mock(QueueBrowser.class);
        final Message message_1 = mock(Message.class);
        final Message message_2 = mock(Message.class);
        final Message message_3 = mock(Message.class);
        final Message message_4 = mock(Message.class);
        final Message message_5 = mock(Message.class);
        final Message message_6 = mock(Message.class);

        final String jmsMessageId_1 = randomUUID().toString();
        final String jmsMessageId_2 = randomUUID().toString();
        final String jmsMessageId_3 = randomUUID().toString();

        when(jmsMessageUtil.getJmsMessageIdFrom(message_1)).thenReturn(jmsMessageId_1);
        when(jmsMessageUtil.getConsumerFrom(message_1)).thenReturn("artemis-manager.command_handler");
        when(jmsMessageUtil.getJmsMessageIdFrom(message_2)).thenReturn(jmsMessageId_1);
        when(jmsMessageUtil.getConsumerFrom(message_2)).thenReturn("artemis-manager.command_handler");
        when(jmsMessageUtil.getJmsMessageIdFrom(message_3)).thenReturn(jmsMessageId_1);
        when(jmsMessageUtil.getConsumerFrom(message_3)).thenReturn("artemis-manager.command_handler");
        when(jmsMessageUtil.getJmsMessageIdFrom(message_4)).thenReturn(jmsMessageId_2);
        when(jmsMessageUtil.getConsumerFrom(message_4)).thenReturn("artemis-manager.command_handler");
        when(jmsMessageUtil.getJmsMessageIdFrom(message_5)).thenReturn(jmsMessageId_3);
        when(jmsMessageUtil.getConsumerFrom(message_5)).thenReturn("artemis-manager.event_listener");
        when(jmsMessageUtil.getJmsMessageIdFrom(message_6)).thenReturn(jmsMessageId_3);
        when(jmsMessageUtil.getConsumerFrom(message_6)).thenReturn("artemis-manager.event_processor");

        final Enumeration<Message> messageEnumeration = enumeration(asList(message_1, message_2, message_3, message_4, message_5, message_6));

        when(queueBrowser.getEnumeration()).thenReturn(messageEnumeration);

        final DuplicateMessages duplicateMessages = duplicateMessageFinder.findDuplicateMessages(queueBrowser);

        final Map<String, Message> duplicateMessagesMap = duplicateMessages.getDuplicateMessages();
        assertThat(duplicateMessagesMap.size(), is(1));

        final Iterator<Message> messageIterator = duplicateMessagesMap.values().iterator();
        assertThat(messageIterator.next(), is(message_1));

        assertThat(duplicateMessages.getMessageCache().size(), is(3));
    }

    @Test
    public void shouldReturnEmptyDuplicatesIfNoDuplicates() throws Exception {

        final QueueBrowser queueBrowser = mock(QueueBrowser.class);
        final Message message_1 = mock(Message.class);
        final Message message_2 = mock(Message.class);
        final Message message_3 = mock(Message.class);

        final String jmsMessageId_1 = randomUUID().toString();
        final String jmsMessageId_2 = randomUUID().toString();
        final String jmsMessageId_3 = randomUUID().toString();

        when(jmsMessageUtil.getJmsMessageIdFrom(message_1)).thenReturn(jmsMessageId_1);
        when(jmsMessageUtil.getConsumerFrom(message_1)).thenReturn("artemis-manager.command_handler");
        when(jmsMessageUtil.getJmsMessageIdFrom(message_2)).thenReturn(jmsMessageId_2);
        when(jmsMessageUtil.getConsumerFrom(message_2)).thenReturn("artemis-manager.command_handler");
        when(jmsMessageUtil.getJmsMessageIdFrom(message_3)).thenReturn(jmsMessageId_3);
        when(jmsMessageUtil.getConsumerFrom(message_3)).thenReturn("artemis-manager.command_handler");

        final Enumeration<Message> messageEnumeration = enumeration(asList(message_1, message_2, message_3));

        when(queueBrowser.getEnumeration()).thenReturn(messageEnumeration);

        final DuplicateMessages duplicateMessages = duplicateMessageFinder.findDuplicateMessages(queueBrowser);

        assertThat(duplicateMessages.getDuplicateMessages().isEmpty(), is(true));
        assertThat(duplicateMessages.getMessageCache().size(), is(3));
    }

    @Test
    public void shouldReturnEmptyIfNoMessages() throws Exception {

        final QueueBrowser queueBrowser = mock(QueueBrowser.class);
        final Enumeration<Message> messageEnumeration = emptyEnumeration();

        when(queueBrowser.getEnumeration()).thenReturn(messageEnumeration);

        final DuplicateMessages duplicateMessages = duplicateMessageFinder.findDuplicateMessages(queueBrowser);

        assertThat(duplicateMessages.getDuplicateMessages().isEmpty(), is(true));
        assertThat(duplicateMessages.getMessageCache().isEmpty(), is(true));
    }

    @Test
    public void shouldThrowCombinedManagementFunctionExceptionIfJMSExceptionIsThrown() throws Exception {

        final QueueBrowser queueBrowser = mock(QueueBrowser.class);
        final JMSException jmsException = mock(JMSException.class);

        when(queueBrowser.getEnumeration()).thenThrow(jmsException);

        try {
            duplicateMessageFinder.findDuplicateMessages(queueBrowser);
            fail();
        } catch (final CombinedManagementFunctionException exception) {
            assertThat(exception.getMessage(), is("Failed to browse messages on queue."));
            assertThat(exception.getCause(), is(jmsException));
        }
    }
}