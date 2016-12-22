package uk.gov.justice.framework.tools.command;


import uk.gov.justice.framework.tools.common.command.ShellCommand;

import com.beust.jcommander.Parameter;

public class Remove extends AbstractArtemisCommand implements ShellCommand {

    @Parameter(names = "-msgId", description = "ip address of artemis node", required = true)
    String msgId;

    @Override
    public void run(final String[] strings) {
        try {
            final boolean removed = artemisConnector.removeMessage(host, port, brokerName, "DLQ", msgId);
            System.out.printf("%s message %s%n", removed ? "Removed" : "Could NOT remove", msgId);
        } catch (final IllegalArgumentException e) {
            System.out.printf("Could NOT remove message %s%n", msgId);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
