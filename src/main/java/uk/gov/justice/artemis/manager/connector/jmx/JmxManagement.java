package uk.gov.justice.artemis.manager.connector.jmx;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import uk.gov.justice.artemis.manager.connector.MessageData;
import uk.gov.justice.output.OutputPrinter;

import java.util.Iterator;
import java.util.List;

import javax.management.openmbean.CompositeData;

public class JmxManagement {

    private static final String JMS_MESSAGE_ID = "JMSMessageID";
    private static final String ORIGINAL_DESTINATION = "OriginalDestination";
    private static final String TEXT = "Text";
    private final OutputPrinter outputPrinter;

    public JmxManagement(final OutputPrinter outputPrinter) {
        this.outputPrinter = outputPrinter;
    }

    public JmxManagementFunction<List<MessageData>> browseMessages() {
        return queueControl -> {
            try {
                final CompositeData[] browseResult = queueControl.browse();

                return stream(browseResult)
                        .map(message -> {
                            final String jmsMessageId = String.valueOf(message.get(JMS_MESSAGE_ID)).replaceFirst("ID:", "");
                            final String originalDestination = String.valueOf(message.get(ORIGINAL_DESTINATION));
                            final String text = String.valueOf(message.get(TEXT));

                            return new MessageData(jmsMessageId, originalDestination, text);
                        })
                        .collect(toList());

            } catch (final Exception exception) {
                throw new JmxManagementFunctionFailedException("JMX Browse messages failed.", exception);
            }
        };
    }

    public JmxManagementFunction<Long> removeMessages(final Iterator<String> msgIds) {
        return queueControl -> {
            long removedMessages = 0;

            while (msgIds.hasNext()) {
                try {
                    queueControl.removeMessage(format("ID:%s", msgIds.next()));
                    removedMessages++;
                } catch (final Exception exception) {
                    outputPrinter.writeException(exception);
                }
            }

            return removedMessages;
        };
    }

    public JmxManagementFunction<Long> reprocessMessages(final Iterator<String> msgIds) {
        return queueControl -> {
            long reprocessedMessages = 0;

            while (msgIds.hasNext()) {
                try {
                    final String nextId = msgIds.next();
                    if (queueControl.retryMessage(format("ID:%s", nextId))) {
                        reprocessedMessages++;
                    } else {
                        outputPrinter.writeException(new RuntimeException(format("Skipped retrying of message id %s as it does not exist", nextId)));
                    }
                } catch (final Exception exception) {
                    outputPrinter.writeException(exception);
                }
            }

            return reprocessedMessages;
        };
    }


    public JmxManagementFunction<Integer> reprocessAllMessages() {
        return queueControl -> {
            int reprocessedMessageCount = 0;
            try {
                reprocessedMessageCount = queueControl.retryMessages();
            } catch (final Exception exception) {
                outputPrinter.writeException(exception);
            }
            return reprocessedMessageCount;
        };
    }
}
