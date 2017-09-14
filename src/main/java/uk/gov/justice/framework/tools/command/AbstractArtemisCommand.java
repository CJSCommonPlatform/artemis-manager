package uk.gov.justice.framework.tools.command;

import uk.gov.justice.artemis.manager.connector.ArtemisConnector;
import uk.gov.justice.artemis.manager.connector.CombinedJmsAndJmxArtemisConnector;
import uk.gov.justice.output.ConsolePrinter;
import uk.gov.justice.output.OutputPrinter;

import com.beust.jcommander.Parameter;

abstract class AbstractArtemisCommand {

    ArtemisConnector artemisConnector = new CombinedJmsAndJmxArtemisConnector();

    final OutputPrinter outputPrinter = new ConsolePrinter();

    @Parameter(names = "-host", description = "ip address of artemis node", required = true)
    String host;

    @Parameter(names = "-port", description = "jmx port", required = true)
    String port;

    @Parameter(names = "-brokerName", description = "broker name as specified in broker.xml", required = true)
    String brokerName;
}
