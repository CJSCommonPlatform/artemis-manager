package uk.gov.justice.artemis.manager.connector.combined.duplicate;

import uk.gov.justice.artemis.manager.connector.combined.CombinedManagementFunctionException;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueBrowser;

public class AddedMessageFinder {

    private final JmsMessageUtil jmsMessageUtil;

    public AddedMessageFinder(final JmsMessageUtil jmsMessageUtil) {
        this.jmsMessageUtil = jmsMessageUtil;
    }

    @SuppressWarnings("unchecked")
    public List<String> findAddedMessages(final BrowsedMessages browsedMessages, final QueueBrowser queueBrowser) {

        final Map<String, Message> messageCache = browsedMessages.getMessageCache();
        final List<String> addedMessageIds = new ArrayList<>();

        try {
            final Enumeration<Message> browserEnumeration = queueBrowser.getEnumeration();

            while (browserEnumeration.hasMoreElements()) {

                final Message message = browserEnumeration.nextElement();
                final String jmsMessageID = jmsMessageUtil.getJmsMessageIdFrom(message);

                if (!messageCache.containsKey(jmsMessageID)) {
                    addedMessageIds.add(jmsMessageID);
                }
            }

            return addedMessageIds;

        } catch (final JMSException exception) {
            throw new CombinedManagementFunctionException("Failed to browse messages on queue.", exception);
        }
    }
}
