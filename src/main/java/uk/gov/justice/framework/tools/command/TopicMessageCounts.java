package uk.gov.justice.framework.tools.command;

import java.util.Map;

import uk.gov.justice.framework.tools.common.command.ShellCommand;

public class TopicMessageCounts extends AbstractArtemisCommand implements ShellCommand {

    @Override
    public void run(final String[] args) {

        try {
            final String[] topics = artemisConnector.topicNames(host, port, brokerName);
            final Map<String, Long> counts = artemisConnector.topicMessageCount(host, port, brokerName, topics);
            outputPrinter.writeMap(counts, "messageCount");
        } catch (final Exception exception) {
            outputPrinter.writeStackTrace(exception);
        }
    }

}
