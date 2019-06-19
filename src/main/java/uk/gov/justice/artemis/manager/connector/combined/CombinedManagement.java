package uk.gov.justice.artemis.manager.connector.combined;

import uk.gov.justice.artemis.manager.connector.combined.duplicate.AddedMessageFinder;
import uk.gov.justice.artemis.manager.connector.combined.duplicate.BrowsedMessages;
import uk.gov.justice.artemis.manager.connector.combined.duplicate.DuplicateMessageFinder;
import uk.gov.justice.artemis.manager.connector.combined.duplicate.DuplicateMessageRemover;
import uk.gov.justice.artemis.manager.connector.combined.duplicate.TopicDuplicateMessageFinder;

import java.util.List;

public class CombinedManagement {

    private final DuplicateMessageFinder duplicateMessageFinder;
    private final TopicDuplicateMessageFinder topicDuplicateMessageFinder;
    private final DuplicateMessageRemover duplicateMessageRemover;
    private final AddedMessageFinder addedMessageFinder;

    public CombinedManagement(final DuplicateMessageFinder duplicateMessageFinder,
                              final TopicDuplicateMessageFinder topicDuplicateMessageFinder,
                              final DuplicateMessageRemover duplicateMessageRemover,
                              final AddedMessageFinder addedMessageFinder) {
        this.duplicateMessageFinder = duplicateMessageFinder;
        this.topicDuplicateMessageFinder = topicDuplicateMessageFinder;
        this.duplicateMessageRemover = duplicateMessageRemover;
        this.addedMessageFinder = addedMessageFinder;
    }

    public CombinedManagementFunction<List<String>> removeAllDuplicates() {

        return (queueBrowser, queueSender, jmsQueueControl) -> {

            final BrowsedMessages browsedMessages = duplicateMessageFinder.findDuplicateMessages(queueBrowser);

            duplicateMessageRemover.removeAndResendDuplicateMessages(
                    queueSender,
                    jmsQueueControl,
                    browsedMessages);

            return addedMessageFinder.findAddedMessages(browsedMessages, queueBrowser);
        };
    }

    public CombinedManagementFunction<List<String>> deduplicateTopicMessages() {
        return (queueBrowser, queueSender, jmsQueueControl) -> {

            final BrowsedMessages topicBrowsedMessages = topicDuplicateMessageFinder.findTopicDuplicateMessages(queueBrowser);

            duplicateMessageRemover.removeAndResendDuplicateMessages(
                    queueSender,
                    jmsQueueControl,
                    topicBrowsedMessages);

            return addedMessageFinder.findAddedMessages(topicBrowsedMessages, queueBrowser);
        };
    }
}
