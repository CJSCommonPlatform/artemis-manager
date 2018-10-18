package uk.gov.justice.artemis.manager.connector.jmx;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import uk.gov.justice.artemis.manager.connector.MessageData;
import uk.gov.justice.output.OutputPrinter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

                    final Map<String, Object>[] listMessages = queueControl.listMessages(format("JMSMessageID = 'ID:%s'", nextId));



//                    final CompositeData[] messages = queueControl.browse("");
//
//                    Arrays.stream(messages)
//                            .filter(compositeData -> filterMessageId(compositeData, nextId))
//                            .forEach(this::outputMessage);
//
//                    final Optional<CompositeData> messageData = Arrays.stream(messages)
//                            .filter(compositeData -> filterMessageId(compositeData, nextId))
//                            .findFirst();

//                    final CompositeData messageValues = messageData.get();

                    final Map<String, String> headers = new HashMap<>();

                    headers.put("_AMQ_ORIG_ADDRESS", listMessages[0].get("_AMQ_ORIG_ADDRESS").toString());

//                    queueControl.moveMessage(format("ID:%s", nextId), "DLQ", true);

                    final String messageId = queueControl.sendTextMessage(headers, "");

//                    queueControl.retryMessage(messageId);
////
//                    if(messages.length > 1) {
//                        for (int index = 1; index < messages.length; index++) {
//                            final String amqOrigQueue = messages[index].get("_AMQ_ORIG_QUEUE").toString();
//
//                            queueControl.removeMessages(format("JMSMessageID = 'ID:%s' AND _AMQ_ORIG_QUEUE = '%s'", nextId, amqOrigQueue));
//                        }
//                    }
//
//                    final CompositeData[] browse = queueControl.browse(format("JMSMessageID = 'ID:%s' AND _AMQ_ORIG_QUEUE = 'artemis-manager.subscription02'", nextId));

//                    final String filter = format("JMSMessageID = 'ID:%s' AND _AMQ_ORIG_QUEUE = 'artemis-manager.subscription02'", nextId);
//
//                    queueControl.removeMessages(filter);

//                    if (queueControl.retryMessage(format("ID:%s", nextId))) {
//                        reprocessedMessages++;
//                    } else {
//                        outputPrinter.writeException(new RuntimeException(format("Skipped retrying of message id %s as it does not exist", nextId)));
//                    }

                } catch (final Exception exception) {
                    outputPrinter.writeException(exception);
                }
            }

            return reprocessedMessages;
        };
    }

    private void outputMessage(final CompositeData compositeData) {
        final Set<String> keys = compositeData.getCompositeType().keySet();

        keys.forEach(key -> outputPrinter.write(key + " = " + compositeData.get(key).toString()));
    }

    private boolean filterMessageId(final CompositeData compositeData, final String nextId) {
        final String jmsMessageID = ((String) compositeData.get("JMSMessageID")).replaceFirst("ID:", "");

        return jmsMessageID.equals(nextId);
    }
}
