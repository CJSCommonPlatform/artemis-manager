package uk.gov.justice.report;

import static java.lang.String.format;

public class ReportGeneratorFactory {

    public static final String TOTALS_BY_NAME_REPORT = "totals-by-name-report";
    public static final String NAMES_BY_ORIGINAL_DESTINATION_REPORT = "names-by-original-destination-report";
    public static final String CREATED_AT_NAME_TOTAL_REPORT = "created-at-name-total-report";

    public ReportGenerator reportGeneratorFor(final String reportType) {

        if (TOTALS_BY_NAME_REPORT.equals(reportType.toLowerCase())) {
            return new TotalsByNameReportGenerator();
        }

        if (NAMES_BY_ORIGINAL_DESTINATION_REPORT.equals(reportType.toLowerCase())) {
            return new NamesByOriginalDestinationReportGenerator();
        }

        if (CREATED_AT_NAME_TOTAL_REPORT.equals(reportType.toLowerCase())) {
            return new CreatedAtNameTotalReportGenerator();
        }

        throw new ReportGeneratorFactoryException(format("Incorrect report type '%s'. Accepted types are: '%s', '%s', '%s'",
                reportType, TOTALS_BY_NAME_REPORT, NAMES_BY_ORIGINAL_DESTINATION_REPORT, CREATED_AT_NAME_TOTAL_REPORT));
    }
}
