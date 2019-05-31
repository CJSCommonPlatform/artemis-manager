package uk.gov.justice.artemis.manager.connector.combined.duplicate;

import java.util.Map;

import javax.jms.Message;

public class DuplicateMessages {

    private final Map<String, Message> duplicateMessages;
    private final Map<String, Message> messageCache;

    public DuplicateMessages(final Map<String, Message> duplicateMessages,
                             final Map<String, Message> messageCache) {
        this.duplicateMessages = duplicateMessages;
        this.messageCache = messageCache;
    }

    public Map<String, Message> getDuplicateMessages() {
        return duplicateMessages;
    }

    public Map<String, Message> getMessageCache() {
        return messageCache;
    }
}
