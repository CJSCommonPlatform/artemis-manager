package uk.gov.justice.artemis.manager.connector.combined.duplicate;

import java.util.List;
import java.util.Map;

import javax.jms.Message;

public class BrowsedMessages {

    private final Map<String, List<Message>> duplicateMessages;
    private final Map<String, Message> messageCache;

    public BrowsedMessages(final Map<String, List<Message>> duplicateMessages, final Map<String, Message> messageCache) {

        this.duplicateMessages = duplicateMessages;
        this.messageCache = messageCache;
    }

    public Map<String, List<Message>> getDuplicateMessages() {
        return duplicateMessages;
    }

    public Map<String, Message> getMessageCache() {
        return messageCache;
    }
}
