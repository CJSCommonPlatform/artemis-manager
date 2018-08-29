package uk.gov.justice.framework.tools.command;


import static java.util.Collections.singletonList;

import uk.gov.justice.framework.tools.common.command.ShellCommand;

import java.util.Iterator;
import java.util.Scanner;

public class Reprocess extends AbstractMsgIdConsumingArtemisCommand implements ShellCommand {

    @Override
    public void run(final String[] strings) {
        try {
            super.setup();
            long reprocessedMessages = reprocessMessagesOf(singleMessageIdProvided() ? singletonList(msgId).iterator() : new Scanner(System.in));
            outputPrinter.writeCommandResult("Reprocess message", reprocessedMessages);
        } catch (final Exception e) {
            outputPrinter.writeStackTrace(e);
        }
    }

    private long reprocessMessagesOf(final Iterator<String> msgIds) {
        try {
            return artemisConnector.reprocess("DLQ", msgIds);
        } catch (final Exception exception) {
            outputPrinter.writeStackTrace(exception);
            return 0;
        }
    }
}
