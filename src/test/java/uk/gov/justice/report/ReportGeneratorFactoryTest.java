package uk.gov.justice.report;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.justice.report.ReportGeneratorFactory.CREATED_AT_NAME_TOTAL_REPORT;
import static uk.gov.justice.report.ReportGeneratorFactory.NAMES_BY_ORIGINAL_DESTINATION_REPORT;
import static uk.gov.justice.report.ReportGeneratorFactory.TOTALS_BY_NAME_REPORT;

import org.junit.Test;

public class ReportGeneratorFactoryTest {

    @Test
    public void shouldCreateTotalsByNameReportGenerator() {

        final ReportGenerator reportGenerator = new ReportGeneratorFactory().reportGeneratorFor(TOTALS_BY_NAME_REPORT);

        assertThat(reportGenerator, instanceOf(TotalsByNameReportGenerator.class));
    }

    @Test
    public void shouldCreateNamesByOriginalDestinationReportGenerator() {

        final ReportGenerator reportGenerator = new ReportGeneratorFactory().reportGeneratorFor(NAMES_BY_ORIGINAL_DESTINATION_REPORT);

        assertThat(reportGenerator, instanceOf(NamesByOriginalDestinationReportGenerator.class));
    }

    @Test
    public void shouldCreateCreatedAtNameTotalReportGenerator() {

        final ReportGenerator reportGenerator = new ReportGeneratorFactory().reportGeneratorFor(CREATED_AT_NAME_TOTAL_REPORT);

        assertThat(reportGenerator, instanceOf(CreatedAtNameTotalReportGenerator.class));
    }

    @Test
    public void shouldThrowExceptionIfUnknownReportGeneratorRequested() {

        try {
            new ReportGeneratorFactory().reportGeneratorFor("unknown-report");
            fail("Exception expected");
        } catch (final ReportGeneratorFactoryException e) {
            assertThat(e.getMessage(), is("Incorrect report type 'unknown-report'. Accepted types are: 'totals-by-name-report', 'names-by-original-destination-report', 'created-at-name-total-report'"));
        }
    }
}