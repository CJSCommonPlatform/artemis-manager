package uk.gov.justice.artemis.manager.connector.combined.duplicate;

import static java.lang.String.format;

import uk.gov.justice.artemis.manager.connector.combined.CombinedManagementFunctionException;

import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueSender;

import org.apache.activemq.artemis.api.jms.management.JMSQueueControl;

public class DuplicateMessageRemover {

    public void removeAndResendDuplicateMessages(final QueueSender queueSender,
                                                 final JMSQueueControl jmsQueueControl,
                                                 final BrowsedMessages browsedMessages) {

        final Map<String, List<Message>> duplicateMessagesMap = browsedMessages.getDuplicateMessages();

        duplicateMessagesMap.keySet()
                .forEach(jmsMessageId -> {

                    try {

                        final String filter = format("JMSMessageID = 'ID:%s'", jmsMessageId);

                        jmsQueueControl.removeMessages(filter);

                        final List<Message> messages = duplicateMessagesMap.get(jmsMessageId);

                        sendMessages(queueSender, jmsMessageId, messages);
                    } catch (final CombinedManagementFunctionException exception) {
                        throw exception;
                    } catch (final Exception exception) {
                        throw new CombinedManagementFunctionException(format("Failed to remove duplicates for JMSMessageID: %s", jmsMessageId), exception);
                    }

                });
    }

    private void sendMessages(final QueueSender queueSender, final String jmsMessageId, final List<Message> messages) {
        messages.forEach(message -> {
            try {
                queueSender.send(message);
            } catch (final JMSException exception) {
                throw new CombinedManagementFunctionException(format("Failed to add message back onto queue, all messages have been deleted for JMSMessageID: %s", jmsMessageId), exception);
            }
        });
    }
}
