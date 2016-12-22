package uk.gov.justice.framework.tools.command;


import uk.gov.justice.artemis.manager.connector.ArtemisConnector;
import uk.gov.justice.artemis.manager.connector.JmxArtemisConnector;
import uk.gov.justice.artemis.manager.connector.MessageData;
import uk.gov.justice.framework.tools.common.command.ShellCommand;

import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;

import com.beust.jcommander.Parameter;

public class Browse implements ShellCommand {

    ArtemisConnector artemisConnector = new JmxArtemisConnector();

    @Parameter(names = "-host", description = "ip address of artemis node", required = true)
    String host;

    @Parameter(names = "-port", description = "jmx port", required = true)
    String port;

    @Parameter(names = "-brokerName", description = "broker name as specified in broker.xml", required = true)
    String brokerName;


    @Override
    public void run(final String[] args) {

        try {
            final List<MessageData> messageData = artemisConnector.messagesOf(host, port, brokerName, "DLQ");
            System.out.println(jsonStringOf(messageData));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String jsonStringOf(final List<MessageData> messageData) {
        final JsonArrayBuilder jsonResponse = Json.createArrayBuilder();
        for (MessageData md : messageData) {
            jsonResponse
                    .add(Json.createObjectBuilder().add("msgId", String.valueOf(md.getMsgId()))
                            .add("originalDestination", String.valueOf(md.getOriginalDestination()))
                            .add("msgContent", md.getMsgContent()));

        }
        return jsonResponse.build().toString();
    }
}