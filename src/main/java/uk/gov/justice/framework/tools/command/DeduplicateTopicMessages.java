package uk.gov.justice.framework.tools.command;

import uk.gov.justice.framework.tools.common.command.ShellCommand;

import java.util.List;

public class DeduplicateTopicMessages extends AbstractArtemisCommand implements ShellCommand {

    @Override
    public void run(final String[] strings) {
        try {
            super.setup();

            final List<String> messageIds = artemisConnector.deduplicateTopicMessages("DLQ");
            outputPrinter.writeStringCollection(messageIds);
        } catch (final Exception exception) {
            outputPrinter.writeStackTrace(exception);
        }
    }
}
