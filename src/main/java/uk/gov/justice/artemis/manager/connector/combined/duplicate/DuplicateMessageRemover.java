package uk.gov.justice.artemis.manager.connector.combined.duplicate;

import static java.lang.String.format;

import uk.gov.justice.artemis.manager.connector.combined.CombinedManagementFunctionException;

import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueSender;

import org.apache.activemq.artemis.api.jms.management.JMSQueueControl;

public class DuplicateMessageRemover {

    public void removeDuplicatesOnly(final QueueSender queueSender,
                                     final JMSQueueControl jmsQueueControl,
                                     final DuplicateMessages duplicateMessages) {

        final Map<String, Message> duplicateMessagesMap = duplicateMessages.getDuplicateMessages();

        duplicateMessagesMap.keySet()
                .forEach(jmsMessageId -> {

                    try {

                        final String filter = format("JMSMessageID = 'ID:%s'", jmsMessageId);

                        jmsQueueControl.removeMessages(filter);
                        queueSender.send(duplicateMessagesMap.get(jmsMessageId));

                    } catch (final JMSException exception) {
                        throw new CombinedManagementFunctionException(format("Failed to add message back onto queue, all messages have been deleted for JMSMessageID: %s", jmsMessageId), exception);
                    } catch (final Exception exception) {
                        throw new CombinedManagementFunctionException(format("Failed to remove duplicates for JMSMessageID: %s", jmsMessageId), exception);
                    }

                });
    }
}
