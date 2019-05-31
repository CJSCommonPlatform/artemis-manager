package uk.gov.justice.framework.tools.command;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.verify;

import uk.gov.justice.artemis.manager.connector.ArtemisConnector;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReportTest {

    @Mock
    private ArtemisConnector artemisConnector;

    @InjectMocks
    private Report report;

    @Test
    public void shouldInvokeConnector() throws Exception {
        report.jmxURLs = singletonList("service:jmx:rmi:///jndi/rmi://localhost:3000/jmxrmi");
        report.jmsURL = "tcp://localhost:61616";
        report.brokerName = "brokerabc";

        report.run(null);
        verify(artemisConnector).messagesOf("DLQ");
    }
}