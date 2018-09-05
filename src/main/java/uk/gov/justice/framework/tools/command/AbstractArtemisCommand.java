package uk.gov.justice.framework.tools.command;

import uk.gov.justice.artemis.manager.connector.ArtemisConnector;
import uk.gov.justice.artemis.manager.connector.CombinedJmsAndJmxArtemisConnector;
import uk.gov.justice.output.ConsolePrinter;
import uk.gov.justice.output.OutputPrinter;

import java.net.MalformedURLException;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.google.common.collect.ImmutableList;

abstract class AbstractArtemisCommand {
    public static final String DEFAULT_JMX_URL = "service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi";
    public static final String DEFAULT_BROKER_NAME = "default";
    public static final String DEFAULT_JMS_URL = "tcp://localhost:61616?clientID=artemis-manager";

    final OutputPrinter outputPrinter = new ConsolePrinter();

    ArtemisConnector artemisConnector = new CombinedJmsAndJmxArtemisConnector();

    @Parameter(names = "-jmxUrl", description = "Full JMX URLs, can be specified mulitple times (default: " + DEFAULT_JMX_URL + ")", variableArity = true)
    List<String> jmxURLs = ImmutableList.of(DEFAULT_JMX_URL);
 
    @Parameter(names = {"-brokerName", "-jmxBrokerName"}, description = "broker name as specified in broker.xml (default: " + DEFAULT_BROKER_NAME + ")")
    String brokerName = DEFAULT_BROKER_NAME;

    @Parameter(names = "-jmxUsername", description = "JMX Username (optional)")
    String jmxUsername;

    @Parameter(names = "-jmxPassword", description = "JMX Password (optional)")
    String jmxPassword;

    @Parameter(names = "-jmsUrl", description = "Full JMS URL (default: " + DEFAULT_JMS_URL + ")")
    String jmsURL = DEFAULT_JMS_URL;

    @Parameter(names = "-jmsUsername", description = "JMS Username (optional)")
    String jmsUsername;

    @Parameter(names = "-jmsPassword", description = "JMS Password (optional)")
    String jmsPassword;

    @Parameter(names = "-help", help = true)
    private boolean help;

    public void setup() throws MalformedURLException {
        artemisConnector.setParameters(this.jmxURLs,
                            this.brokerName,
                            this.jmxUsername,
                            this.jmxPassword,
                            this.jmsURL,
                            this.jmsUsername,
                            this.jmsPassword);
    }
}