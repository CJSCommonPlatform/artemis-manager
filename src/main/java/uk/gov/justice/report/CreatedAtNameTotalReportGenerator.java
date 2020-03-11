package uk.gov.justice.report;

import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;

import uk.gov.justice.artemis.manager.connector.MessageData;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.messaging.JsonObjects;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.json.JsonObject;
import javax.json.JsonString;

public class CreatedAtNameTotalReportGenerator implements ReportGenerator {

    @Override
    public String generate(final List<MessageData> messageData) {

        final Map<LocalDate, Map<String, TotalByName>> createdAtNameTotals = new HashMap<>();

        messageData.forEach(message -> {
            final JsonObject msgContent = message.getMsgContent();

            final Optional<JsonString> nameJson = JsonObjects.getJsonString(msgContent, "_metadata", "name");
            final Optional<JsonString> createdAtJson = JsonObjects.getJsonString(msgContent, "_metadata", "createdAt");

            if (createdAtJson.isPresent()) {

                final LocalDate createdAtDate = ZonedDateTimes.fromJsonString(createdAtJson.get()).toLocalDate();

                if (nameJson.isPresent()) {
                    final String name = nameJson.get().getString();

                    final Map<String, TotalByName> totalsByName = createdAtNameTotals.computeIfAbsent(createdAtDate, key -> new HashMap<>());
                    final TotalByName totalByName = totalsByName.computeIfAbsent(name, name1 -> new TotalByName(createdAtDate, name1));

                    totalByName.addOneToTotal();
                }
            }
        });

        final String csv = createdAtNameTotals.keySet().stream()
                .sorted()
                .map(localDate -> createdAtNameTotals.get(localDate).values().stream()
                        .sorted()
                        .map(TotalByName::toString)
                        .collect(joining(lineSeparator())))
                .collect(joining(lineSeparator()));

        return "Created At,Name,Total Messages"
                + lineSeparator()
                + csv
                + lineSeparator()
                + "Total Messages,," + (long) messageData.size();
    }

    private static class TotalByName implements Comparable<TotalByName> {

        private final LocalDate createdAt;
        private final String name;
        private Long totalMessages = 0L;

        TotalByName(final LocalDate createdAt, final String name) {
            this.createdAt = createdAt;
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
            return createdAt + "," + name + "," + totalMessages;
        }

        @Override
        public int compareTo(final TotalByName other) {
            return totalMessages.compareTo(other.getTotalMessages());
        }
    }
}
