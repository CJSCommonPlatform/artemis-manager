package uk.gov.justice.artemis.manager.connector.combined.duplicate;

import uk.gov.justice.artemis.manager.connector.combined.CombinedManagementFunctionException;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueBrowser;

public class DuplicateMessageFinder {

    private final JmsMessageUtil jmsMessageUtil;

    public DuplicateMessageFinder(final JmsMessageUtil jmsMessageUtil) {
        this.jmsMessageUtil = jmsMessageUtil;
    }

    @SuppressWarnings("unchecked")
    public DuplicateMessages findDuplicateMessages(final QueueBrowser queueBrowser) {

        final Map<String, Message> duplicateMessages = new HashMap<>();
        final Map<String, Message> messageCache = new HashMap<>();

        try {
            final Enumeration<Message> browserEnumeration = queueBrowser.getEnumeration();

            while (browserEnumeration.hasMoreElements()) {

                final Message message = browserEnumeration.nextElement();
                final String jmsMessageID = jmsMessageUtil.getJmsMessageIdFrom(message);

                if (messageCache.containsKey(jmsMessageID) && !duplicateMessages.containsKey(jmsMessageID)) {

                    final Message originalMessage = messageCache.get(jmsMessageID);
                    final String originalConsumer = jmsMessageUtil.getConsumerFrom(originalMessage);
                    final String duplicateConsumer = jmsMessageUtil.getConsumerFrom(message);
                    final boolean isNotDuplicateTopicMessage = duplicateConsumer.equals(originalConsumer);

                    if (isNotDuplicateTopicMessage) {
                        duplicateMessages.put(jmsMessageID, originalMessage);
                    }

                } else {
                    messageCache.put(jmsMessageID, message);
                }
            }

            return new DuplicateMessages(duplicateMessages, messageCache);

        } catch (final JMSException exception) {
            throw new CombinedManagementFunctionException("Failed to browse messages on queue.", exception);
        }
    }
}
