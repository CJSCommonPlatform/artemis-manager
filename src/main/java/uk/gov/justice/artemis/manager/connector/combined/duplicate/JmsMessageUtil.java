package uk.gov.justice.artemis.manager.connector.combined.duplicate;


import javax.jms.JMSException;
import javax.jms.Message;

public class JmsMessageUtil {

    private static final String ID_PREFIX = "ID:";
    private static final String BLANK = "";
    private static final String CONSUMER = "_AMQ_ORIG_QUEUE";

    public String getJmsMessageIdFrom(final Message message) throws JMSException {
        return message.getJMSMessageID().replaceFirst(ID_PREFIX, BLANK);
    }

    public String getConsumerFrom(final Message message) throws JMSException {
        return message.getStringProperty(CONSUMER);
    }
}
