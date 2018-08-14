package uk.gov.justice.output;

import uk.gov.justice.artemis.manager.connector.MessageData;

import java.util.List;

public interface OutputPrinter {

    /**
     * Writes a standard message to the output.
     * @param message - the message to be written.
     */
    public void write(final String message);

    /**
     * Writes information about an executed command to the output.
     * @param command - the command that was performed.
     * @param count - the number of times the command was performed successfully.
     */
    public void writeCommandResult(final String command, final long count);

    /**
     * Writes a list of Strings to the output in JSON Array style
     * @param items - the list of items to be written
     */
    public void writeStringArray(final String[] items);

    /**
     * Writes a list of {@link MessageData} content to the output.
     * @param messageData - the list of data to be written.
     */
    public void writeMessages(final List<MessageData> messageData);

    /**
     * Writes the stack trace related with an exception to the output.
     * @param exception - the exception to print the stack trace for
     */
    public void writeStackTrace(final Exception exception);

    /**
     * Writes the exception details (not full stack trace) to the output.
     * @param throwable - the exception to be written.
     */
    public void writeException(final Throwable throwable);

}
