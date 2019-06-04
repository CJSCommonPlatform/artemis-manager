package uk.gov.justice.artemis.manager.connector.combined;

import uk.gov.justice.artemis.manager.connector.combined.duplicate.AddedMessageFinder;
import uk.gov.justice.artemis.manager.connector.combined.duplicate.DuplicateMessageFinder;
import uk.gov.justice.artemis.manager.connector.combined.duplicate.DuplicateMessageRemover;
import uk.gov.justice.artemis.manager.connector.combined.duplicate.DuplicateMessages;

import java.util.List;

public class CombinedManagement {

    private final DuplicateMessageFinder duplicateMessageFinder;
    private final DuplicateMessageRemover duplicateMessageRemover;
    private final AddedMessageFinder addedMessageFinder;

    public CombinedManagement(final DuplicateMessageFinder duplicateMessageFinder,
                              final DuplicateMessageRemover duplicateMessageRemover,
                              final AddedMessageFinder addedMessageFinder) {
        this.duplicateMessageFinder = duplicateMessageFinder;
        this.duplicateMessageRemover = duplicateMessageRemover;
        this.addedMessageFinder = addedMessageFinder;
    }

    public CombinedManagementFunction<List<String>> removeAllDuplicates() {

        return (queueBrowser, queueSender, jmsQueueControl) ->
        {
            final DuplicateMessages duplicateMessages = duplicateMessageFinder.findDuplicateMessages(queueBrowser);

            duplicateMessageRemover.removeDuplicatesOnly(
                    queueSender,
                    jmsQueueControl,
                    duplicateMessages);

            return addedMessageFinder.findAddedMessages(duplicateMessages, queueBrowser);
        };
    }
}
