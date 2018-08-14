package uk.gov.justice.framework.tools.command;

import uk.gov.justice.framework.tools.common.command.ShellCommand;

public class ListQueues extends AbstractArtemisCommand implements ShellCommand {

    @Override
    public void run(String[] args) {

        try {
            final String[] queues = artemisConnector.queueNames(host, port, brokerName);
            outputPrinter.writeStringArray(queues);
        } catch (final Exception exception) {
            outputPrinter.writeStackTrace(exception);
        }
    }

}
