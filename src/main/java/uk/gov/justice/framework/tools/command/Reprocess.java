package uk.gov.justice.framework.tools.command;


import static java.util.Collections.singletonList;

import uk.gov.justice.framework.tools.common.command.ShellCommand;

import java.util.Iterator;
import java.util.Scanner;

public class Reprocess extends AbstractMsgIdConsumingArtemisCommand implements ShellCommand {

    @Override
    public void run(final String[] strings) {
        long reprocessedMessages = reprocessMessagesOf(singleMessageIdProvided() ? singletonList(msgId).iterator() : new Scanner(System.in));
        outputPrinter.writeCommandResult("Reprocess message", reprocessedMessages);
    }

    private long reprocessMessagesOf(final Iterator<String> msgIds) {
        try {
            return artemisConnector.reprocess(host, port, brokerName, "DLQ", msgIds);
        } catch (final Exception exception) {
            outputPrinter.writeStackTrace(exception);
            return 0;
        }
    }
}
