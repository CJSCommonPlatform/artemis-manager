package uk.gov.justice.artemis.manager;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.justice.artemis.manager.util.JmsTestUtil.cleanQueue;
import static uk.gov.justice.artemis.manager.util.JmsTestUtil.closeJmsConnection;
import static uk.gov.justice.artemis.manager.util.JmsTestUtil.consumerOf;
import static uk.gov.justice.artemis.manager.util.JmsTestUtil.openJmsConnection;
import static uk.gov.justice.artemis.manager.util.JmsTestUtil.putInQueue;
import static uk.gov.justice.artemis.manager.util.JmsTestUtil.putInQueueWithMessageId;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.jms.JMSException;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;

import com.jayway.jsonpath.JsonPath;
import com.opencsv.CSVReader;
import org.apache.activemq.artemis.utils.UUID;
import org.apache.activemq.artemis.utils.UUIDGenerator;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

//to run this test from IDE start artemis first by executing ./target/server0/bin/artemis run
public class ArtemisManagerIT {

    private static final String DLQ = "DLQ";
    private static final String COMMAND_LINE_BROWSE = "env -u _JAVA_OPTIONS java -jar target/artemis-manager.jar browse -brokerName 0.0.0.0 -jmxUrl service:jmx:rmi:///jndi/rmi://localhost:3000/jmxrmi -jmsUrl tcp://localhost:61616?clientID=artemis-manager";
    private static final String COMMAND_LINE_REPORT = "env -u _JAVA_OPTIONS java -jar target/artemis-manager.jar report -reportType %s -brokerName 0.0.0.0 -jmxUrl service:jmx:rmi:///jndi/rmi://localhost:3000/jmxrmi -jmsUrl tcp://localhost:61616?clientID=artemis-manager";
    private static final String COMMAND_LINE_REPROCESS = "env -u _JAVA_OPTIONS java -jar target/artemis-manager.jar reprocess -brokerName 0.0.0.0 -jmxUrl service:jmx:rmi:///jndi/rmi://localhost:3000/jmxrmi -jmsUrl tcp://localhost:61616?clientID=artemis-manager";
    private static final String COMMAND_LINE_REMOVE = "env -u _JAVA_OPTIONS java -jar target/artemis-manager.jar remove -brokerName 0.0.0.0 -jmxUrl service:jmx:rmi:///jndi/rmi://localhost:3000/jmxrmi -jmsUrl tcp://localhost:61616?clientID=artemis-manager";
    private static final String COMMAND_LINE_REMOVE_ALL_DUPLICATES = "env -u _JAVA_OPTIONS java -jar target/artemis-manager.jar removeallduplicates -brokerName 0.0.0.0 -jmxUrl service:jmx:rmi:///jndi/rmi://localhost:3000/jmxrmi -jmsUrl tcp://localhost:61616?clientID=artemis-manager";
    private static final String COMMAND_LINE_DEDUPLICATE_TOPIC_MESSAGES = "env -u _JAVA_OPTIONS java -jar target/artemis-manager.jar deduplicatetopicmessages -brokerName 0.0.0.0 -jmxUrl service:jmx:rmi:///jndi/rmi://localhost:3000/jmxrmi -jmsUrl tcp://localhost:61616?clientID=artemis-manager";

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

        putInQueue(DLQ, "{\"_metadata\":{\"name\":\"some.name\",\"id\":\"c97c5b7b-abc3-49d4-96a9-bcd83aa4ea12\"}}", "consumer1", "jms.queue.abracadabra");
        putInQueue(DLQ, "{\"_metadata\":{\"name\":\"some.other.name\",\"id\":\"c97c5b7b-abc3-49d4-96a9-bcd83aa4ea13\"}}", "consumer2", "jms.queue.hocuspocus");

        final Output output = execute(COMMAND_LINE_BROWSE);
        assertThat(output.errorOutput, isEmptyString());

        final String standardOutput = output.standardOutput();

        assertThat(standardOutput, hasJsonPath("$..msgId", hasSize(2)));
        assertThat(standardOutput, hasJsonPath("$[0].msgId"));
        assertThat(standardOutput, hasJsonPath("$[0].originalDestination", equalTo("jms.queue.abracadabra")));
        assertThat(standardOutput, hasJsonPath("$[0].msgContent._metadata.name", equalTo("some.name")));
        assertThat(standardOutput, hasJsonPath("$[0].msgContent._metadata.id", equalTo("c97c5b7b-abc3-49d4-96a9-bcd83aa4ea12")));
        assertThat(standardOutput, hasJsonPath("$[0].consumer", equalTo("consumer1")));

        assertThat(standardOutput, hasJsonPath("$[1].msgId"));
        assertThat(standardOutput, hasJsonPath("$[1].originalDestination", equalTo("jms.queue.hocuspocus")));
        assertThat(standardOutput, hasJsonPath("$[1].msgContent._metadata.name", equalTo("some.other.name")));
        assertThat(standardOutput, hasJsonPath("$[1].msgContent._metadata.id", equalTo("c97c5b7b-abc3-49d4-96a9-bcd83aa4ea13")));
        assertThat(standardOutput, hasJsonPath("$[1].consumer", equalTo("consumer2")));
    }

    @Test
    public void shouldGenerateTotalsByNameReportWithDLQ() throws Exception {
        cleanQueue(DLQ);

        putInQueue(DLQ, "{\"_metadata\":{\"name\":\"some.name\",\"id\":\"c97c5b7b-abc3-49d4-96a9-bcd83aa4ea12\"}}", "consumer1", "jms.queue.abracadabra");
        putInQueue(DLQ, "{\"_metadata\":{\"name\":\"some.other.name\",\"id\":\"c97c5b7b-abc3-49d4-96a9-bcd83aa4ea13\"}}", "consumer2", "jms.queue.hocuspocus");

        final Output output = execute(String.format(COMMAND_LINE_REPORT, "totals-by-name-report"));
        assertThat(output.errorOutput, isEmptyString());

        final String standardOutput = output.standardOutput();

        final CSVReader reader = new CSVReader(new StringReader(standardOutput));

        final List<String[]> lines = reader.readAll();

        assertThat(lines.get(0)[0], is("Name"));
        assertThat(lines.get(0)[1], is("Total Messages"));

        assertThat(lines.get(1)[0], is("some.other.name"));
        assertThat(lines.get(1)[1], is("1"));

        assertThat(lines.get(2)[0], is("some.name"));
        assertThat(lines.get(2)[1], is("1"));

        assertThat(lines.get(3)[0], is("Total Messages"));
        assertThat(lines.get(3)[1], is("2"));
    }

    @Test
    public void shouldGenerateNamesByOriginalDestinationReportWithDLQ() throws Exception {
        cleanQueue(DLQ);

        putInQueue(DLQ, "{\"_metadata\":{\"name\":\"some.name\",\"id\":\"c97c5b7b-abc3-49d4-96a9-bcd83aa4ea12\"}}", "consumer1", "jms.queue.abracadabra");
        putInQueue(DLQ, "{\"_metadata\":{\"name\":\"some.other.name\",\"id\":\"c97c5b7b-abc3-49d4-96a9-bcd83aa4ea13\"}}", "consumer2", "jms.queue.hocuspocus");

        final Output output = execute(String.format(COMMAND_LINE_REPORT, "names-by-original-destination-report"));
        assertThat(output.errorOutput, isEmptyString());

        final String standardOutput = output.standardOutput();

        final CSVReader reader = new CSVReader(new StringReader(standardOutput));

        final List<String[]> lines = reader.readAll();

        assertThat(lines.get(0)[0], is("Original Destination"));
        assertThat(lines.get(0)[1], is("Name"));

        assertThat(lines.get(1)[0], is("jms.queue.abracadabra"));
        assertThat(lines.get(1)[1], is("some.name"));

        assertThat(lines.get(2)[0], is("jms.queue.hocuspocus"));
        assertThat(lines.get(2)[1], is("some.other.name"));
    }

    @Test
    public void shouldGenerateCreatedAtNameTotalReportWithDLQ() throws Exception {
        cleanQueue(DLQ);

        putInQueue(DLQ, "{\"_metadata\":{\"name\":\"some.name\",\"createdAt\":\"2019-04-06T16:32:48.725Z\",\"id\":\"c97c5b7b-abc3-49d4-96a9-bcd83aa4ea12\"}}", "consumer1", "jms.queue.abracadabra");
        putInQueue(DLQ, "{\"_metadata\":{\"name\":\"some.other.name\",\"createdAt\":\"2019-04-06T16:32:48.725Z\",\"id\":\"c97c5b7b-abc3-49d4-96a9-bcd83aa4ea13\"}}", "consumer2", "jms.queue.hocuspocus");

        final Output output = execute(String.format(COMMAND_LINE_REPORT, "created-at-name-total-report"));
        assertThat(output.errorOutput, isEmptyString());

        final String standardOutput = output.standardOutput();

        final CSVReader reader = new CSVReader(new StringReader(standardOutput));

        final List<String[]> lines = reader.readAll();

        assertThat(lines.get(0)[0], is("Created At"));
        assertThat(lines.get(0)[1], is("Name"));
        assertThat(lines.get(0)[2], is("Total Messages"));

        assertThat(lines.get(1)[0], is("2019-04-06"));
        assertThat(lines.get(1)[1], is("some.other.name"));
        assertThat(lines.get(1)[2], is("1"));

        assertThat(lines.get(2)[0], is("2019-04-06"));
        assertThat(lines.get(2)[1], is("some.name"));
        assertThat(lines.get(2)[2], is("1"));

        assertThat(lines.get(3)[0], is("Total Messages"));
        assertThat(lines.get(3)[1], is(""));
        assertThat(lines.get(3)[2], is("2"));
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
            putInQueue(DLQ, "{\"_metadata\":{\"name\":\"some.name2\"}}", "consumer1", "jms.queue.abracadabra2");


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
    public void shouldRemoveAllDuplicateMessagesAndReturnListOfRemovedMessageIds() throws Exception {
        if (notWindows()) {

            final UUID jmsMessageId_1 = UUIDGenerator.getInstance().generateUUID();
            final UUID jmsMessageId_2 = UUIDGenerator.getInstance().generateUUID();
            final UUID jmsMessageId_3 = UUIDGenerator.getInstance().generateUUID();
            final String messageText = "{\"key1\":\"value123\"}";
            final String consumer_1 = "consumer1";
            final String originalQueue_1 = "origQueueO1";

            cleanQueue(DLQ);

            putInQueueWithMessageId(DLQ, jmsMessageId_1, messageText, consumer_1, originalQueue_1);
            putInQueueWithMessageId(DLQ, jmsMessageId_1, messageText, consumer_1, originalQueue_1);
            putInQueueWithMessageId(DLQ, jmsMessageId_1, messageText, consumer_1, originalQueue_1);
            putInQueueWithMessageId(DLQ, jmsMessageId_2, messageText, consumer_1, originalQueue_1);
            putInQueueWithMessageId(DLQ, jmsMessageId_3, messageText, consumer_1, originalQueue_1);
            putInQueueWithMessageId(DLQ, jmsMessageId_3, messageText, consumer_1, originalQueue_1);

            final Output output = execute(COMMAND_LINE_REMOVE_ALL_DUPLICATES);
            assertThat(output.errorOutput, isEmptyString());

            final String standardOutput = output.standardOutput();

            final JsonReader reader = Json.createReader(new StringReader(standardOutput));
            final JsonArray jsonArray = reader.readArray();

            assertThat(jsonArray.size(), is(2));
            assertDLQHasSizeOf(3);
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

    @Test
    public void shouldReprocessLargeMessageOntoOriginalQueue() throws Exception {
        if (notWindows()) {

            cleanQueue(DLQ);
            cleanQueue("abracadabra");
            cleanQueue("hocuspocus");

            // Create consumers so that the queues are created within the broker
            consumerOf("abracadabra");
            consumerOf("hocuspocus");

            putInQueue(DLQ, createLargeMessage(4024L), "consumer1", "jms.queue.abracadabra");
            putInQueue(DLQ, "{\"_metadata\":{\"name\":\"some.other.name\"}}", "consumer2", "jms.queue.hocuspocus");

            assertDLQHasSizeOf(2);

            final String messageData = standardOutputOf(COMMAND_LINE_BROWSE);
            final List<String> msgIds = JsonPath.read(messageData, "$[*].msgId");

            final String shellCommand2 = "echo " + msgIds.get(0) + " " + msgIds.get(1) + " | " + COMMAND_LINE_REPROCESS;
            final Output output = execute(new String[]{"/bin/sh", "-c", shellCommand2});

            assertThat(output.standardOutput(), is("{\"Command\":\"Reprocess message\",\"Occurrences\":2}\n"));

            assertDLQHasSizeOf(0);
            assertThat(cleanQueue("abracadabra"), equalTo(1));
            assertThat(cleanQueue("hocuspocus"), equalTo(1));
        }
    }

    @Test
    public void shouldDeduplicateTopicMessagesAndReturnListOfMessageIds() throws Exception {
        if (notWindows()) {

            final UUID jmsMessageId_1 = UUIDGenerator.getInstance().generateUUID();
            final UUID jmsMessageId_2 = UUIDGenerator.getInstance().generateUUID();
            final UUID jmsMessageId_3 = UUIDGenerator.getInstance().generateUUID();
            final String messageText_1 = "{\"key1\":\"value1\"}";
            final String messageText_2 = "{\"key1\":\"value2\"}";
            final String consumer_1 = "consumer1";
            final String consumer_2 = "consumer2";
            final String consumer_3 = "consumer3";
            final String originalQueue_1 = "origQueueO1";
            final String originalQueue_2 = "origQueueO2";

            cleanQueue(DLQ);

            putInQueueWithMessageId(DLQ, jmsMessageId_1, messageText_1, consumer_1, originalQueue_1);
            putInQueueWithMessageId(DLQ, jmsMessageId_1, messageText_1, consumer_1, originalQueue_1);
            putInQueueWithMessageId(DLQ, jmsMessageId_1, messageText_1, consumer_1, originalQueue_1);
            putInQueueWithMessageId(DLQ, jmsMessageId_2, messageText_1, consumer_1, originalQueue_1);
            putInQueueWithMessageId(DLQ, jmsMessageId_3, messageText_2, consumer_1, originalQueue_2);
            putInQueueWithMessageId(DLQ, jmsMessageId_3, messageText_2, consumer_2, originalQueue_2);
            putInQueueWithMessageId(DLQ, jmsMessageId_3, messageText_2, consumer_3, originalQueue_2);

            final Output output = execute(COMMAND_LINE_DEDUPLICATE_TOPIC_MESSAGES);
            assertThat(output.errorOutput, isEmptyString());

            final String standardOutput = output.standardOutput();

            final JsonReader reader = Json.createReader(new StringReader(standardOutput));
            final JsonArray jsonArray = reader.readArray();

            assertThat(jsonArray.size(), is(3));
            assertDLQHasSizeOf(7);

            jsonArray.forEach(jsonValue -> {
                try {
                    execute(COMMAND_LINE_REMOVE + " -msgId " + jsonValue.toString());
                } catch (final IOException e) {
                    fail("Failed to remove: " + jsonValue.toString());
                }
            });

            assertDLQHasSizeOf(4);
        }
    }

    private void setDefaultDLQMessages() throws JMSException, IOException {
        cleanQueue(DLQ);

        putInQueue(DLQ, "{\"_metadata\":{\"name\":\"some.name\"}}", "consumer1", "jms.queue.abracadabra");
        putInQueue(DLQ, "{\"_metadata\":{\"name\":\"some.other.name\"}}", "consumer2", "jms.queue.hocuspocus");

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

    private String createLargeMessage(final long messageSize) throws IOException {
        final StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("{\n  \"_metadata\": {\n");

        for (long index = 0L; index < messageSize - 1; index++) {
            stringBuilder.append("      \"name")
                    .append(index)
                    .append("\": \"some name\",\n");
        }

        stringBuilder.append("      \"name")
                .append(messageSize)
                .append("\": \"some name\"\n")
                .append("  }\n}");

        return stringBuilder.toString();
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
