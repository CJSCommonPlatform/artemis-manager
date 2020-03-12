package uk.gov.justice.framework.tools.command;


import uk.gov.justice.artemis.manager.connector.filehandling.MessageFileException;
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

        try {
            super.setup();

            if(textMessageFile == null || textMessageFile.isEmpty()) {
                throw new MessageFileException("Cannot read message file. No file name set. Please use -messageFile parameter when calling this command");
            }

            final String message = textMessageFileContentsReader.readContentsOf(textMessageFile);

            final String results = artemisConnector.sendTextMessage("DLQ", message);
            outputPrinter.write(results);

        } catch (final Exception exception) {
            outputPrinter.writeStackTrace(exception);
        }
    }
}
