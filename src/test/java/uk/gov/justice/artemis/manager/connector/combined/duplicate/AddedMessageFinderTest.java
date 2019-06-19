package uk.gov.justice.artemis.manager.connector.combined.duplicate;

import static java.util.Arrays.asList;
import static java.util.Collections.enumeration;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.artemis.manager.connector.combined.CombinedManagementFunctionException;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueBrowser;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AddedMessageFinderTest {

    @Mock
    private JmsMessageUtil jmsMessageUtil;

    @InjectMocks
    private AddedMessageFinder addedMessageFinder;

    @Test
    public void shouldFindAddedMessages() throws Exception {

        final QueueBrowser queueBrowser = mock(QueueBrowser.class);
        final Map<String, Message> messageCache = new HashMap<>();

        final String messageId_1 = randomUUID().toString();
        final String messageId_2 = randomUUID().toString();
        final String messageId_3 = randomUUID().toString();
        final String messageId_4 = randomUUID().toString();

        final Message message_1 = mock(Message.class);
        final Message message_2 = mock(Message.class);
        final Message message_3 = mock(Message.class);
        final Message message_4 = mock(Message.class);

        messageCache.put(messageId_1, message_1);
        messageCache.put(messageId_3, message_3);

        final Enumeration<Message> messageEnumeration = enumeration(asList(message_1, message_2, message_3, message_4));

        when(queueBrowser.getEnumeration()).thenReturn(messageEnumeration);
        when(jmsMessageUtil.getJmsMessageIdFrom(message_1)).thenReturn(messageId_1);
        when(jmsMessageUtil.getJmsMessageIdFrom(message_2)).thenReturn(messageId_2);
        when(jmsMessageUtil.getJmsMessageIdFrom(message_3)).thenReturn(messageId_3);
        when(jmsMessageUtil.getJmsMessageIdFrom(message_4)).thenReturn(messageId_4);

        final List<String> addedMessageIds = addedMessageFinder.findAddedMessages(
                new BrowsedMessages(null, messageCache),
                queueBrowser);

        assertThat(addedMessageIds.size(), is(2));
        assertThat(addedMessageIds, hasItems(messageId_2, messageId_4));
    }

    @Test
    public void shouldThrowCombinedManagementFunctionExceptionIfJMSExceptionIsThrown() throws Exception {

        final QueueBrowser queueBrowser = mock(QueueBrowser.class);
        final JMSException jmsException = mock(JMSException.class);

        when(queueBrowser.getEnumeration()).thenThrow(jmsException);

        try {
            addedMessageFinder.findAddedMessages(mock(BrowsedMessages.class), queueBrowser);
            fail();
        } catch (final CombinedManagementFunctionException exception) {
            Assert.assertThat(exception.getMessage(), is("Failed to browse messages on queue."));
            Assert.assertThat(exception.getCause(), is(jmsException));
        }
    }
}