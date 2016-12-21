package uk.gov.justice.artemis.manager.connector;

import java.util.List;

public interface ArtemisConnector {
    List<MessageData> messagesOf(final String host, final String port, final String brokerName, final String destinationName) throws Exception;
}
