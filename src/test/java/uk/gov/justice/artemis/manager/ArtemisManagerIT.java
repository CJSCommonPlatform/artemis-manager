package uk.gov.justice.artemis.manager;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.artemis.manager.util.JmsTestUtil.cleanQueue;
import static uk.gov.justice.artemis.manager.util.JmsTestUtil.closeJmsConnection;
import static uk.gov.justice.artemis.manager.util.JmsTestUtil.consumerOf;
import static uk.gov.justice.artemis.manager.util.JmsTestUtil.openJmsConnection;
import static uk.gov.justice.artemis.manager.util.JmsTestUtil.putInQueue;

import java.io.IOException;
import java.util.List;

import javax.jms.JMSException;

import com.jayway.jsonpath.JsonPath;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

//to run this test from IDE start artemis first by executing ./target/server0/bin/artemis run
public class ArtemisManagerIT {
    
    private static final String DLQ = "DLQ";
    private static final String COMMAND_LINE_BROWSE = "java -jar target/artemis-manager.jar browse -host localhost -port 3000 -brokerName 0.0.0.0";
    private static final String COMMAND_LINE_REPROCESS = "java -jar target/artemis-manager.jar reprocess -host localhost -port 3000 -brokerName 0.0.0.0";
    private static final String COMMAND_LINE_REMOVE = "java -jar target/artemis-manager.jar remove -host localhost -port 3000 -brokerName 0.0.0.0";

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

        final Output output = execute(COMMAND_LINE_BROWSE);
        assertThat(output.errorOutput, isEmptyString());

        final String standardOutput = output.standardOutput();

        assertThat(standardOutput, hasJsonPath("$..msgId", hasSize(2)));
        assertThat(standardOutput, hasJsonPath("$[0].msgId"));
        assertThat(standardOutput, hasJsonPath("$[0].originalDestination", equalTo("jms.queue.abracadabra")));
        assertThat(standardOutput, hasJsonPath("$[0].msgContent._metadata.name", equalTo("some.name")));
        assertThat(standardOutput, hasJsonPath("$[0].msgContent._metadata.id", equalTo("c97c5b7b-abc3-49d4-96a9-bcd83aa4ea12")));

        assertThat(standardOutput, hasJsonPath("$[1].msgId"));
        assertThat(standardOutput, hasJsonPath("$[1].originalDestination", equalTo("jms.queue.hocuspocus")));
        assertThat(standardOutput, hasJsonPath("$[1].msgContent._metadata.name", equalTo("some.other.name")));
        assertThat(standardOutput, hasJsonPath("$[1].msgContent._metadata.id", equalTo("c97c5b7b-abc3-49d4-96a9-bcd83aa4ea13")));

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
    public void shouldRemoveMessageById() throws Exception {

        setDefaultDLQMessages();

        final String messageData = standardOutputOf(COMMAND_LINE_BROWSE);

        final List<String> msgIds = JsonPath.read(messageData, "$[*].msgId");
        assertThat(msgIds, hasSize(2));

        final String msgIdToRemove = msgIds.get(0);
        execute(COMMAND_LINE_REMOVE + " -msgId " + msgIdToRemove);

        final String messageDataAfterRemoval = standardOutputOf(COMMAND_LINE_BROWSE);

        assertThat(messageDataAfterRemoval, hasJsonPath("$..msgId", hasSize(1)));
        assertThat(messageDataAfterRemoval, hasJsonPath("$[0].msgId", equalTo(msgIds.get(1))));

    }

    @Test
    public void shouldReturnInfoIfMessageNotound() throws IOException {

        final Output output = execute(COMMAND_LINE_REMOVE + " -msgId 1234");

        assertThat(output.errorOutput(), is("No message found for JMSMessageID: ID:1234\n"));
    }

    @Test
    public void shouldRemoveMultipleMessagesReadingIdsFromSystemInput() throws Exception {
        if (notWindows()) {
            setDefaultDLQMessages();
            putInQueue(DLQ, "{\"_metadata\":{\"name\":\"some.name2\"}}", "jms.queue.abracadabra2");


            final String messageData = standardOutputOf(COMMAND_LINE_BROWSE);

            final List<String> msgIds = JsonPath.read(messageData, "$[*].msgId");

            final String shellCommand = "echo " + msgIds.get(0) + " " + msgIds.get(2) + " | " + COMMAND_LINE_REMOVE;
            final Output output = execute(new String[]{"/bin/sh", "-c", shellCommand});
            assertThat(output.errorOutput(), is(""));

            final String messageDataAfterRemoval = standardOutputOf(COMMAND_LINE_BROWSE);

            assertThat(messageDataAfterRemoval, hasJsonPath("$..msgId", hasSize(1)));
            assertThat(messageDataAfterRemoval, hasJsonPath("$[0].msgId", equalTo(msgIds.get(1))));

        }
    }

    @Test
    public void shouldIgnoreUnknownMessageIds() throws Exception {
        if (notWindows()) {
            setDefaultDLQMessages();

            final String messageData = standardOutputOf(COMMAND_LINE_BROWSE);

            final List<String> msgIds = JsonPath.read(messageData, "$[*].msgId");

            final String shellCommand = "echo unknown_id123 " + msgIds.get(0) + " | " + COMMAND_LINE_REMOVE;
            execute(new String[]{"/bin/sh", "-c", shellCommand});

            final String messageDataAfterRemoval = standardOutputOf(COMMAND_LINE_BROWSE);

            assertThat(messageDataAfterRemoval, hasJsonPath("$..msgId", hasSize(1)));
            assertThat(messageDataAfterRemoval, hasJsonPath("$[0].msgId", equalTo(msgIds.get(1))));
        }
    }

    @Test
    public void shouldOutputNumberOfRemovedMessages() throws JMSException, IOException {

        if (notWindows()) {
            setDefaultDLQMessages();

            final String messageData = standardOutputOf(COMMAND_LINE_BROWSE);

            final List<String> msgIds = JsonPath.read(messageData, "$[*].msgId");

            final String shellCommand = "echo unknown_id123 " + msgIds.get(0) + " " + msgIds.get(1) + " | " + COMMAND_LINE_REMOVE;
            final Output output = execute(new String[]{"/bin/sh", "-c", shellCommand});

            assertThat(output.standardOutput(), is("{\"Command\":\"Remove message\",\"Occurrences\":2}\n"));
        }
    }

    @Test
    public void shouldReprocessMessageOntoOriginalQueue() throws JMSException, IOException {
        if (notWindows()) {
            setDefaultDLQMessages();

            // Create consumers so that the queues are created within the broker
            consumerOf("abracadabra");
            consumerOf("hocuspocus");

            final String messageData = standardOutputOf(COMMAND_LINE_BROWSE);

            final List<String> msgIds = JsonPath.read(messageData, "$[*].msgId");

            final String shellCommand = "echo unknown_id123 " + msgIds.get(0) + " " + msgIds.get(1) + " | " + COMMAND_LINE_REPROCESS;
            final Output output = execute(new String[]{"/bin/sh", "-c", shellCommand});

            assertThat(output.standardOutput(), is("{\"Command\":\"Reprocess message\",\"Occurrences\":2}\n"));
            assertThat(output.errorOutput, equalTo("Skipped retrying of message id unknown_id123 as it does not exist\n"));

            assertDLQHasSizeOf(0);
            assertThat(cleanQueue("abracadabra"), equalTo(1));
            assertThat(cleanQueue("hocuspocus"), equalTo(1));
        }
    }

    private void setDefaultDLQMessages() throws JMSException, IOException {
        cleanQueue(DLQ);

        putInQueue(DLQ, "{\"_metadata\":{\"name\":\"some.name\"}}", "jms.queue.abracadabra");
        putInQueue(DLQ, "{\"_metadata\":{\"name\":\"some.other.name\"}}", "jms.queue.hocuspocus");

        assertDLQHasSizeOf(2);
    }

    private void assertDLQHasSizeOf(int queueSize) throws IOException {
        final Output browserOutput = execute(COMMAND_LINE_BROWSE);

        assertThat(browserOutput.standardOutput(), hasJsonPath("$..msgId", hasSize(queueSize)));
    }

    private boolean notWindows() {
        return !System.getProperty("os.name").startsWith("Windows");
    }

    private String standardOutputOf(final String cmd) throws IOException {
        return execute(cmd).standardOutput();
    }

    private String errorOutputOf(final String cmd) throws IOException {
        return execute(cmd).errorOutput();
    }


    private Output execute(final String[] cmd) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        final Process process = runtime.exec(cmd);
        return new Output(IOUtils.toString(process.getInputStream()), IOUtils.toString(process.getErrorStream()));
    }

    private Output execute(final String cmd) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        final Process process = runtime.exec(cmd);
        return new Output(IOUtils.toString(process.getInputStream()), IOUtils.toString(process.getErrorStream()));
    }

    private static class Output {
        private String standardOutput;
        private String errorOutput;

        public Output(final String standardOutput, final String errorOutput) {
            this.standardOutput = standardOutput;
            this.errorOutput = errorOutput;
        }

        public String standardOutput() {
            return standardOutput;
        }

        public String errorOutput() {
            return errorOutput;
        }
    }
}