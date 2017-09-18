package uk.gov.justice.artemis.manager.connector;


import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class MessageData {
    private String msgId;
    private String originalDestination;
    private JsonObject msgContent;

    public MessageData(final String msgId, final String originalDestination, final String msgText) {
        this.msgId = msgId;
        this.originalDestination = originalDestination;

        try(final JsonReader jsonReader = Json.createReader(new StringReader(String.valueOf(msgText)))) {
            this.msgContent = jsonReader.readObject();
        }
    }

    public String getMsgId() {
        return msgId;
    }

    public String getOriginalDestination() {
        return originalDestination;
    }

    public JsonObject getMsgContent() {
        return msgContent;
    }
}
