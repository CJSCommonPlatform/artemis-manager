package uk.gov.justice.report;

import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;

import uk.gov.justice.artemis.manager.connector.MessageData;
import uk.gov.justice.services.messaging.JsonObjects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import javax.json.JsonObject;
import javax.json.JsonString;

public class NamesByOriginalDestinationReportGenerator implements ReportGenerator {

    public String generate(final List<MessageData> messageData) {

        final Map<String, NameByOriginalDestination> messageTypesByOriginalDestination = new HashMap<>();

        messageData.forEach(message -> {
            final JsonObject msgContent = message.getMsgContent();

            final Optional<JsonString> nameJson = JsonObjects.getJsonString(msgContent, "_metadata", "name");
            final String originalDestination = message.getOriginalDestination();

            if (nameJson.isPresent()) {
                final String name = nameJson.get().getString();
                final NameByOriginalDestination nameByOriginalDestination = messageTypesByOriginalDestination.computeIfAbsent(originalDestination, NameByOriginalDestination::new);

                nameByOriginalDestination.addMessageType(name);
            }
        });

        final String csv = messageTypesByOriginalDestination.values().stream()
                .sorted()
                .map(NameByOriginalDestination::toString)
                .collect(joining());

        return "Original Destination,Name" + lineSeparator() + csv;
    }

    private class NameByOriginalDestination implements Comparable<NameByOriginalDestination> {

        private final String originalDestination;
        private final Set<String> names = new TreeSet<>();

        NameByOriginalDestination(final String originalDestination) {
            this.originalDestination = originalDestination;
        }

        void addMessageType(final String messageType) {
            names.add(messageType);
        }

        @Override
        public int compareTo(final NameByOriginalDestination other) {
            return originalDestination.compareTo(other.originalDestination);
        }

        @Override
        public String toString() {
            final StringBuilder stringBuilder = new StringBuilder();

            for (String messageType : names) {
                stringBuilder
                        .append(originalDestination)
                        .append(",")
                        .append(messageType)
                        .append(lineSeparator());
            }

            return stringBuilder.toString();
        }
    }
}
