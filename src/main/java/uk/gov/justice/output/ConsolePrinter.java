package uk.gov.justice.output;

import uk.gov.justice.artemis.manager.connector.MessageData;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
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
    public void writeStringCollection(final Collection<String> items) {
        System.out.println(jsonStringOf(items));
    }

    private JsonObject toJsonObject(Map.Entry<String, Long> entry, String valueName) {
        final JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("name", entry.getKey());
        builder.add(valueName, entry.getValue());
        return builder.build();
    }

    @Override
    public void writeMap(final Map<String, Long> map, String valueName) {
        final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        map.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(
            entry -> arrayBuilder.add(this.toJsonObject(entry, valueName))
        );
        System.out.println(arrayBuilder.build().toString());
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

    private String jsonStringOf(final Collection<String> items) {
        final JsonArrayBuilder jsonResponse = Json.createArrayBuilder();
        items.stream().forEach(jsonResponse::add);
        return jsonResponse.build().toString();
    }

    private String jsonStringOf(final String command, final long count) {

        JsonObjectBuilder response = Json.createObjectBuilder().add("Command", command)
                .add("Occurrences", count);

        return response.build().toString();
    }

}
