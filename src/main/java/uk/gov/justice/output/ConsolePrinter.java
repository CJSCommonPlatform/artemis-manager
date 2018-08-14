package uk.gov.justice.output;

import uk.gov.justice.artemis.manager.connector.MessageData;

import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

public class ConsolePrinter implements OutputPrinter {

    @Override
    public void write(final String message) {
        System.out.println(message);
    }

    @Override
    public void writeCommandResult(final String command, final long count) {
        System.out.println(jsonStringOf(command, count));
    }

    @Override
    public void writeStringArray(final String[] items) {
        System.out.println(jsonStringOf(items));
    }

    @Override
    public void writeMessages(final List<MessageData> messageData) {
        System.out.println(jsonStringOf(messageData));
    }

    @Override
    public void writeStackTrace(final Exception exception) {
        exception.printStackTrace();
    }

    @Override
    public void writeException(final Throwable throwable) {
        System.err.println(throwable.getMessage());
    }

    private String jsonStringOf(final List<MessageData> messageData) {
        final JsonArrayBuilder jsonResponse = Json.createArrayBuilder();
        for (MessageData md : messageData) {
            jsonResponse
                    .add(Json.createObjectBuilder().add("msgId", String.valueOf(md.getMsgId()))
                            .add("originalDestination", String.valueOf(md.getOriginalDestination()))
                            .add("msgContent", md.getMsgContent()));

        }
        return jsonResponse.build().toString();
    }

    private String jsonStringOf(final String[] items) {
        final JsonArrayBuilder jsonResponse = Json.createArrayBuilder();
        for (String item : items) {
            jsonResponse.add(item);
        }
        return jsonResponse.build().toString();
    }

    private String jsonStringOf(final String command, final long count) {

        JsonObjectBuilder response = Json.createObjectBuilder().add("Command", command)
                .add("Occurrences", count);

        return response.build().toString();
    }

}
