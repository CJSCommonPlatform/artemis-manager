package uk.gov.justice.framework.tools.command;


import static java.util.Collections.singletonList;

import uk.gov.justice.framework.tools.common.command.ShellCommand;

import java.util.Iterator;
import java.util.Scanner;

import com.beust.jcommander.Parameter;

public class Reprocess extends AbstractArtemisCommand implements ShellCommand {

    @Parameter(names = "-msgId")
    String msgId;

    @Override
    public void run(final String[] strings) {
        long reprocessedMessages = reprocessMessagesOf(singleMessageIdProvided() ? singletonList(msgId).iterator() : new Scanner(System.in));
        System.out.printf("Reprocessed %d messages%n", reprocessedMessages);
    }

    private boolean singleMessageIdProvided() {
        return msgId != null && !"".equals(msgId.trim());
    }

    private long reprocessMessagesOf(final Iterator<String> msgIds) {
        try {
            return artemisConnector.reprocess(host, port, brokerName, "DLQ", msgIds);
        } catch (final Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
