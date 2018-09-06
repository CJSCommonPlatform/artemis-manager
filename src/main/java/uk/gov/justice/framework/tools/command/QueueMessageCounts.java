package uk.gov.justice.framework.tools.command;

import uk.gov.justice.framework.tools.common.command.ShellCommand;

import java.util.List;
import java.util.Map;

public class QueueMessageCounts extends AbstractArtemisCommand implements ShellCommand {

    @Override
    public void run(final String[] args) {

        try {
            super.setup();
            final List<String> queues = artemisConnector.queueNames();
            final Map<String, Long> counts = artemisConnector.queueMessageCount(queues);
            outputPrinter.writeMap(counts, "messageCount");
        } catch (final Exception exception) {
            outputPrinter.writeStackTrace(exception);
        }
    }

}
