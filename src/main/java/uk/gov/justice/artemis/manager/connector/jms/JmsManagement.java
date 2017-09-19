package uk.gov.justice.artemis.manager.connector.jms;

import uk.gov.justice.artemis.manager.connector.MessageData;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

public class JmsManagement {

    private static final String JMS_ORIGINAL_DESTINATION = "_AMQ_ORIG_ADDRESS";
    private static final String ID_PREFIX = "ID:";
    private static final String BLANK = "";
    private static final String UNSUPPORTED_MESSAGE_CONTENT = "{\"error\": \"Unsupported message content\"}";

    public JmsManagementFunction<List<MessageData>> browseMessages() {
        return queueBrowser -> {
            try {
                final Enumeration browserEnumeration = queueBrowser.getEnumeration();

                final ArrayList<MessageData> messages = new ArrayList<>();

                while (browserEnumeration.hasMoreElements()) {
                    final Message message = (Message) browserEnumeration.nextElement();

                    final String jmsMessageID = message.getJMSMessageID().replaceFirst(ID_PREFIX, BLANK);
                    final String originalDestination = message.getStringProperty(JMS_ORIGINAL_DESTINATION);
                    final String text;

                    if (message instanceof TextMessage) {
                        final TextMessage textMessage = (TextMessage) message;
                        text = textMessage.getText();
                    } else {
                        text = UNSUPPORTED_MESSAGE_CONTENT;
                    }

                    messages.add(new MessageData(jmsMessageID, originalDestination, text));
                }

                return messages;
            } catch (final JMSException exception) {
                throw new JmsManagementFunctionFailedException("JMS Browse messages failed.", exception);
            }
        };
    }
}
