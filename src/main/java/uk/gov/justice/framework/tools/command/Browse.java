package uk.gov.justice.framework.tools.command;


import uk.gov.justice.artemis.manager.connector.MessageData;
import uk.gov.justice.framework.tools.common.command.ShellCommand;

import java.util.List;

public class Browse extends AbstractArtemisCommand implements ShellCommand {

    @Override
    public void run(final String[] args) {

        try {
            final List<MessageData> messageData = artemisConnector.messagesOf(host, port, brokerName, "DLQ");
            outputPrinter.writeMessages(messageData);
        } catch (final Exception exception) {
            outputPrinter.writeStackTrace(exception);
        }
    }
}