package uk.gov.justice.report;

import static java.lang.System.lineSeparator;
import static java.util.Collections.reverseOrder;
import static java.util.stream.Collectors.joining;

import uk.gov.justice.artemis.manager.connector.MessageData;
import uk.gov.justice.services.messaging.JsonObjects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.json.JsonObject;
import javax.json.JsonString;

public class TotalsByNameReportGenerator implements ReportGenerator {

    public String generate(final List<MessageData> messageData) {

        final Map<String, TotalByName> totalsByMessageType = new HashMap<>();

        messageData.forEach(message -> {
            final JsonObject msgContent = message.getMsgContent();

            final Optional<JsonString> nameJson = JsonObjects.getJsonString(msgContent, "_metadata", "name");

            if (nameJson.isPresent()) {
                final String name = nameJson.get().getString();
                final TotalByName totalByName = totalsByMessageType.computeIfAbsent(name, TotalByName::new);

                totalByName.addOneToTotal();
            }
        });

        final String csv = totalsByMessageType.values().stream()
                .sorted(reverseOrder())
                .map(TotalByName::toString)
                .collect(joining(lineSeparator()));

        return "Name,Total Messages"
                + lineSeparator()
                + csv
                + lineSeparator()
                + "Total Messages," + messageData.size();
    }

    private class TotalByName implements Comparable<TotalByName> {

        private final String name;
        private Long totalMessages = 0L;

        TotalByName(final String name) {
            this.name = name;
        }

        Long getTotalMessages() {
            return totalMessages;
        }

        void addOneToTotal() {
            totalMessages = totalMessages + 1L;
        }

        @Override
        public String toString() {
            return name + "," + totalMessages;
        }

        @Override
        public int compareTo(final TotalByName other) {
            return totalMessages.compareTo(other.getTotalMessages());
        }
    }
}
