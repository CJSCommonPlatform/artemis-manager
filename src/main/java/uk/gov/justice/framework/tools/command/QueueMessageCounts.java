package uk.gov.justice.framework.tools.command;

import java.util.Map;

import uk.gov.justice.framework.tools.common.command.ShellCommand;

public class QueueMessageCounts extends AbstractArtemisCommand implements ShellCommand {

    @Override
    public void run(final String[] args) {

        try {
            final String[] queues = artemisConnector.queueNames(host, port, brokerName);
            final Map<String, Long> counts = artemisConnector.queueMessageCount(host, port, brokerName, queues);
            outputPrinter.writeMap(counts, "messageCount");
        } catch (final Exception exception) {
            outputPrinter.writeStackTrace(exception);
        }
    }

}
