package uk.gov.justice.framework.tools.command;

import static uk.gov.justice.report.ReportGeneratorFactory.CREATED_AT_NAME_TOTAL_REPORT;

import uk.gov.justice.artemis.manager.connector.MessageData;
import uk.gov.justice.framework.tools.common.command.ShellCommand;
import uk.gov.justice.report.ReportGeneratorFactory;

import java.util.List;

import com.beust.jcommander.Parameter;

public class Report extends AbstractArtemisCommand implements ShellCommand {

    @Parameter(names = "-reportType", description = CREATED_AT_NAME_TOTAL_REPORT)
    String reportType;

    @Override
    public void run(final String[] args) {
        try {
            super.setup();

            final List<MessageData> messageData = artemisConnector.messagesOf("DLQ");

            final String report = new ReportGeneratorFactory().reportGeneratorFor(reportType).generate(messageData);

            outputPrinter.write(report);
        } catch (final Exception exception) {
            outputPrinter.writeStackTrace(exception);
        }
    }
}
