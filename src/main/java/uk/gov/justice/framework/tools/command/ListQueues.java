package uk.gov.justice.framework.tools.command;

import uk.gov.justice.framework.tools.common.command.ShellCommand;

import java.util.List;

public class ListQueues extends AbstractArtemisCommand implements ShellCommand {

    @Override
    public void run(final String[] args) {

        try {
            super.setup();
            final List<String> queues = artemisConnector.queueNames();
            outputPrinter.writeStringCollection(queues);
        } catch (final Exception exception) {
            outputPrinter.writeStackTrace(exception);
        }
    }

}
