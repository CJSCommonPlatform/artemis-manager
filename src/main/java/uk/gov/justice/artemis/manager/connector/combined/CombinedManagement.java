package uk.gov.justice.artemis.manager.connector.combined;

import static java.lang.String.format;
import static java.util.Collections.list;
import static java.util.stream.Collectors.toList;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.TextMessage;

public class CombinedManagement {

    public CombinedFunction<Long> reprocessMultipleMessages(final Iterator<String> msgIds) {
        return (queueSession, queueBrowser, queueSender, queueControl) -> {
            long reprocessedMessages = 0;

            while (msgIds.hasNext()) {
                try {

                    final String nextId = msgIds.next();

                    //Get the duplicate messages for an Id
                    final Enumeration<TextMessage> queueBrowserEnumeration = queueBrowser.getEnumeration();
                    final List<TextMessage> textMessages = list(queueBrowserEnumeration).stream()
                            .filter(textMessage -> {
                                try {
                                    final String jmsMessageID = textMessage.getJMSMessageID();

                                    return jmsMessageID.equals(format("ID:%s", nextId));
                                } catch (final JMSException e) {
                                    throw new RuntimeException("Aaaaaggggh", e);
                                }
                            })
                            .collect(toList());

                    //Remove from DLQ
                    textMessages.forEach(textMessage -> {
                        try {
                            final String filter = format("JMSMessageID = 'ID:%s'", nextId);
                            queueControl.removeMessages(filter);
                        } catch (final Exception e) {
                            throw new RuntimeException("Aaaaaggggh part 2", e);
                        }
                    });

                    //Add single message back on DLQ
                    textMessages.forEach(textMessage -> {
                        try {
                            queueSender.send(textMessage);
                            final boolean retryMessage = queueControl.retryMessage(textMessage.getJMSMessageID());

                            System.out.println("******** = " + retryMessage);
                        } catch (final Exception e) {
                            throw new RuntimeException("Uuuuugggh", e);
                        }
                    });

                } catch (final Exception exception) {
                    throw new RuntimeException("Failed completely", exception);
                }
            }

            return reprocessedMessages;
        };
    }
}
