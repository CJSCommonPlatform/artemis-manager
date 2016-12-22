package uk.gov.justice.artemis.manager;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.artemis.manager.util.JmsTestUtil.cleanQueue;
import static uk.gov.justice.artemis.manager.util.JmsTestUtil.closeJmsConnection;
import static uk.gov.justice.artemis.manager.util.JmsTestUtil.openJmsConnection;
import static uk.gov.justice.artemis.manager.util.JmsTestUtil.putInQueue;

import java.io.IOException;
import java.util.List;

import javax.jms.JMSException;

import com.jayway.jsonpath.JsonPath;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

//to run this test from IDE start artemis first by executing ./target/server0/bin/artemis run
public class ArtemisManagerIT {
    public static final String DLQ = "DLQ";


    @BeforeClass
    public static void beforeClass() throws JMSException {
        openJmsConnection();
    }

    @AfterClass
    public static void afterClass() throws JMSException {
        closeJmsConnection();
    }

    @Test
    public void shouldBrowseMessagesInDLQ() throws Exception {
        cleanQueue(DLQ);

        putInQueue(DLQ, "{\"_metadata\":{\"name\":\"some.name\",\"id\":\"c97c5b7b-abc3-49d4-96a9-bcd83aa4ea12\"}}", "jms.queue.abracadabra");
        putInQueue(DLQ, "{\"_metadata\":{\"name\":\"some.other.name\",\"id\":\"c97c5b7b-abc3-49d4-96a9-bcd83aa4ea13\"}}", "jms.queue.hocuspocus");


        final String output = standardOutputOf("java -jar target/artemis-manager.jar browse -host localhost -port 3000 -brokerName 0.0.0.0");

        assertThat(output, hasJsonPath("$..msgId", hasSize(2)));
        assertThat(output, hasJsonPath("$[0].msgId"));
        assertThat(output, hasJsonPath("$[0].originalDestination", equalTo("jms.queue.abracadabra")));
        assertThat(output, hasJsonPath("$[0].msgContent._metadata.name", equalTo("some.name")));
        assertThat(output, hasJsonPath("$[0].msgContent._metadata.id", equalTo("c97c5b7b-abc3-49d4-96a9-bcd83aa4ea12")));

        assertThat(output, hasJsonPath("$[1].msgId"));
        assertThat(output, hasJsonPath("$[1].originalDestination", equalTo("jms.queue.hocuspocus")));
        assertThat(output, hasJsonPath("$[1].msgContent._metadata.name", equalTo("some.other.name")));
        assertThat(output, hasJsonPath("$[1].msgContent._metadata.id", equalTo("c97c5b7b-abc3-49d4-96a9-bcd83aa4ea13")));

    }

    @Test
    public void shouldThrowExceptionIfHostMissingWhenBrowsing() throws IOException {
        assertThat(errorOutputOf("java -jar target/artemis-manager.jar browse  -port 3000 -brokerName 0.0.0.0"),
                containsString("The following option is required: -host"));
    }


    @Test
    public void shouldThrowExceptionIfPortMissingWhenBrowsing() throws IOException {
        assertThat(errorOutputOf("java -jar target/artemis-manager.jar browse -host localhost -brokerName 0.0.0.0"),
                containsString("The following option is required: -port"));
    }

    @Test
    public void shouldThrowExceptionIfBrokerNameMissingWhenBrowsing() throws IOException {
        assertThat(errorOutputOf("java -jar target/artemis-manager.jar browse -host localhost -port 3000"),
                containsString("The following option is required: -brokerName"));
    }


    @Test
    @Ignore
    public void shouldRemoveMessageById() throws Exception {

        cleanQueue(DLQ);

        putInQueue(DLQ, "{\"_metadata\":{\"name\":\"some.name\"}}", "jms.queue.abracadabra");
        putInQueue(DLQ, "{\"_metadata\":{\"name\":\"some.other.name\"}}", "jms.queue.hocuspocus");

        final String messageData = standardOutputOf("java -jar target/artemis-manager.jar browse -host localhost -port 3000 -brokerName 0.0.0.0");

        List<String> msgIds = JsonPath.read(messageData, "$[*].msgId");
        assertThat(msgIds, hasSize(2));

        final String output = standardOutputOf("java -jar target/artemis-manager.jar remove -host localhost -port 3000 -brokerName 0.0.0.0 -msgId=" + msgIds.get(0));

        final String messageDataAfterRemoval = standardOutputOf("java -jar target/artemis-manager.jar browse -host localhost -port 3000 -brokerName 0.0.0.0");

        assertThat(messageDataAfterRemoval, hasJsonPath("$..msgId", hasSize(1)));
        assertThat(messageDataAfterRemoval, hasJsonPath("$[0].msgId", equalTo( msgIds.get(1))));

    }

    private String standardOutputOf(final String cmd) throws IOException {
        Process process = execute(cmd);
        return IOUtils.toString(process.getInputStream());
    }

    private String errorOutputOf(final String cmd) throws IOException {
        Process process = execute(cmd);
        return IOUtils.toString(process.getErrorStream());
    }


    private Process execute(final String cmd) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        return runtime.exec(cmd);
    }
}