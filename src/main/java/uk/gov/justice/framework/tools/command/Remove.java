package uk.gov.justice.framework.tools.command;


import static java.util.Collections.singletonList;

import uk.gov.justice.framework.tools.common.command.ShellCommand;

import java.util.Iterator;
import java.util.Scanner;

public class Remove extends AbstractMsgIdConsumingArtemisCommand implements ShellCommand {

    @Override
    public void run(final String[] strings) {
        try {
          super.setup();
          long removedMessages = removeMessagesOf(singleMessageIdProvided() ? singletonList(msgId).iterator() : new Scanner(System.in));
          outputPrinter.writeCommandResult("Remove message", removedMessages);
        } catch(final Exception e) {
            outputPrinter.writeStackTrace(e);
        }
    }

    private long removeMessagesOf(final Iterator<String> msgIds) {
        try {
            return artemisConnector.remove("DLQ", msgIds);
        } catch (final Exception exception) {
            outputPrinter.writeStackTrace(exception);
            return 0;
        }
    }
}
