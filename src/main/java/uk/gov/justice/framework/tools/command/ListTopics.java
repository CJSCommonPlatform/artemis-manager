package uk.gov.justice.framework.tools.command;

import uk.gov.justice.framework.tools.common.command.ShellCommand;

public class ListTopics extends AbstractArtemisCommand implements ShellCommand {

    @Override
    public void run(final String[] args) {

        try {
            final String[] queues = artemisConnector.topicNames(host, port, brokerName);
            outputPrinter.writeStringArray(queues);
        } catch (final Exception exception) {
            outputPrinter.writeStackTrace(exception);
        }
    }

}
