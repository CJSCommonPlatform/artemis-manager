package uk.gov.justice.artemis.manager.connector.combined.duplicate;

import uk.gov.justice.artemis.manager.connector.combined.CombinedManagementFunctionException;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueBrowser;

public class TopicDuplicateMessageFinder {

    private final JmsMessageUtil jmsMessageUtil;

    public TopicDuplicateMessageFinder(final JmsMessageUtil jmsMessageUtil) {
        this.jmsMessageUtil = jmsMessageUtil;
    }

    @SuppressWarnings("unchecked")
    public BrowsedMessages findTopicDuplicateMessages(final QueueBrowser queueBrowser) {

        final Map<String, List<Message>> duplicateMessages = new HashMap<>();
        final Map<String, Message> messageCache = new HashMap<>();

        try {
            final Enumeration<Message> browserEnumeration = queueBrowser.getEnumeration();

            while (browserEnumeration.hasMoreElements()) {

                final Message message = browserEnumeration.nextElement();
                final String jmsMessageID = jmsMessageUtil.getJmsMessageIdFrom(message);

                if (messageCache.containsKey(jmsMessageID)) {

                    final Message originalMessage = messageCache.get(jmsMessageID);
                    final String originalConsumer = jmsMessageUtil.getConsumerFrom(originalMessage);
                    final String duplicateConsumer = jmsMessageUtil.getConsumerFrom(message);
                    final boolean isDuplicateTopicMessage = !duplicateConsumer.equals(originalConsumer);

                    if (isDuplicateTopicMessage) {

                        duplicateMessages
                                .computeIfAbsent(jmsMessageID, key -> createNewMessageListWith(originalMessage))
                                .add(message);
                    } else {
                        messageCache.put(jmsMessageID, message);
                    }

                } else {
                    messageCache.put(jmsMessageID, message);
                }
            }

            return new BrowsedMessages(duplicateMessages, messageCache);

        } catch (final JMSException exception) {
            throw new CombinedManagementFunctionException("Failed to browse messages on queue.", exception);
        }
    }

    private List<Message> createNewMessageListWith(final Message originalMessage) {
        final ArrayList<Message> messages = new ArrayList<>();
        messages.add(originalMessage);
        return messages;
    }
}
