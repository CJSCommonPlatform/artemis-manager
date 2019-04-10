package uk.gov.justice.report;

import uk.gov.justice.artemis.manager.connector.MessageData;

import java.util.List;

public interface ReportGenerator {

    String generate(final List<MessageData> messageData);
}
