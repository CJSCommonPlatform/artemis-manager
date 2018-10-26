package uk.gov.justice.framework.tools.command;


import uk.gov.justice.framework.tools.common.command.ShellCommand;

public class ReprocessAll extends AbstractMsgIdConsumingArtemisCommand implements ShellCommand {

    @Override
    public void run(final String[] strings) {
        try {
            super.setup();
            int reprocessedMessageCount = reprocessMessages();
            outputPrinter.writeCommandResult("Reprocess all messages", reprocessedMessageCount);
        } catch (final Exception e) {
            outputPrinter.writeStackTrace(e);
        }
    }

    private int reprocessMessages() {
        try {
            return artemisConnector.reprocessAll("DLQ");
        } catch (final Exception exception) {
            outputPrinter.writeStackTrace(exception);
            return 0;
        }
    }
}
