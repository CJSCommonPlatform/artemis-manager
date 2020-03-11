package uk.gov.justice.framework.tools.command;


import uk.gov.justice.artemis.manager.connector.filehandling.TextMessageFileContentsReader;
import uk.gov.justice.framework.tools.common.command.ShellCommand;

public class SendMessage extends AbstractArtemisCommand implements ShellCommand {

    private final TextMessageFileContentsReader textMessageFileContentsReader;

    public SendMessage() {
        this(new TextMessageFileContentsReader());
    }

    public SendMessage(final TextMessageFileContentsReader textMessageFileContentsReader) {
        this.textMessageFileContentsReader = textMessageFileContentsReader;
    }

    @Override
    public void run(final String[] args) {

        System.out.println(textMessageFile);
        final String message = textMessageFileContentsReader.readContentsOf(textMessageFile);
        System.out.println(message);

        System.out.println(jmsPassword);
        System.out.println(jmsUsername);

        try {
            super.setup();

            final String results = artemisConnector.sendTextMessage("DLQ", message);
            outputPrinter.write(results);

        } catch (final Exception exception) {
            outputPrinter.writeStackTrace(exception);
        }
    }
}
