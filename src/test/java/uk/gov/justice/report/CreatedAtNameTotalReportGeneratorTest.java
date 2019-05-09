package uk.gov.justice.report;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import uk.gov.justice.artemis.manager.connector.MessageData;

import java.io.StringReader;
import java.util.List;
import java.util.UUID;

import com.opencsv.CSVReader;
import org.junit.Test;

public class CreatedAtNameTotalReportGeneratorTest {

    @Test
    public void shouldCreateReportFromMessageData() throws Exception {

        final UUID streamId_1 = UUID.randomUUID();
        final UUID streamId_2 = UUID.randomUUID();
        final UUID streamId_3 = UUID.randomUUID();

        final String name_1 = "example.command.add-recipe";
        final String name_2 = "example.command.delete-recipe";
        final String name_3 = "example.command.update-recipe";

        final String message_1 = "{\"_metadata\":{\"createdAt\":\"2019-04-06T16:32:48.725Z\",\"id\":\"00b78ee7-5d2c-4b6d-858e-0701d0412515\",\"name\":\"" + name_1 + "\",\"causation\":[\"63bc8905-2705-41f9-892e-946e632b1837\"],\"stream\":{\"id\":\"" + streamId_1 + "\"}}}";
        final String message_2 = "{\"_metadata\":{\"createdAt\":\"2019-04-06T16:32:48.725Z\",\"id\":\"00b78ee7-5d2c-4b6d-858e-0701d0412515\",\"name\":\"" + name_1 + "\",\"causation\":[\"63bc8905-2705-41f9-892e-946e632b1837\"],\"stream\":{\"id\":\"" + streamId_2 + "\"}}}";
        final String message_3 = "{\"_metadata\":{\"createdAt\":\"2019-04-07T16:32:48.725Z\",\"id\":\"00b78ee7-5d2c-4b6d-858e-0701d0412515\",\"name\":\"" + name_3 + "\",\"causation\":[\"63bc8905-2705-41f9-892e-946e632b1837\"],\"stream\":{\"id\":\"" + streamId_2 + "\"}}}";
        final String message_4 = "{\"_metadata\":{\"createdAt\":\"2019-04-07T16:32:48.725Z\",\"id\":\"00b78ee7-5d2c-4b6d-858e-0701d0412515\",\"name\":\"" + name_1 + "\",\"causation\":[\"63bc8905-2705-41f9-892e-946e632b1837\"],\"stream\":{\"id\":\"" + streamId_3 + "\"}}}";
        final String message_5 = "{\"_metadata\":{\"createdAt\":\"2019-04-08T16:32:48.725Z\",\"id\":\"00b78ee7-5d2c-4b6d-858e-0701d0412515\",\"name\":\"" + name_2 + "\",\"causation\":[\"63bc8905-2705-41f9-892e-946e632b1837\"],\"stream\":{\"id\":\"" + streamId_1 + "\"}}}";
        final String message_6 = "{\"_metadata\":{\"createdAt\":\"2019-04-08T16:32:48.725Z\",\"id\":\"00b78ee7-5d2c-4b6d-858e-0701d0412515\",\"name\":\"" + name_3 + "\",\"causation\":[\"63bc8905-2705-41f9-892e-946e632b1837\"],\"stream\":{\"id\":\"" + streamId_3 + "\"}}}";

        final List<MessageData> messageData = asList(
                new MessageData(randomUUID().toString(), "queue.example1", message_1, "consumer"),
                new MessageData(randomUUID().toString(), "queue.example2", message_2, "consumer"),
                new MessageData(randomUUID().toString(), "queue.example2", message_3, "consumer"),
                new MessageData(randomUUID().toString(), "queue.example3", message_4, "consumer"),
                new MessageData(randomUUID().toString(), "queue.example1", message_5, "consumer"),
                new MessageData(randomUUID().toString(), "queue.example3", message_6, "consumer")
        );

        final String csv = new CreatedAtNameTotalReportGenerator().generate(messageData);

        final CSVReader reader = new CSVReader(new StringReader(csv));

        final List<String[]> lines = reader.readAll();

        assertThat(lines.get(0)[0], is("Created At"));
        assertThat(lines.get(0)[1], is("Name"));
        assertThat(lines.get(0)[2], is("Total Messages"));

        assertThat(lines.get(1)[0], is("2019-04-06"));
        assertThat(lines.get(1)[1], is(name_1));
        assertThat(lines.get(1)[2], is("2"));

        assertThat(lines.get(2)[0], is("2019-04-07"));
        assertThat(lines.get(2)[1], is(name_1));
        assertThat(lines.get(2)[2], is("1"));

        assertThat(lines.get(3)[0], is("2019-04-07"));
        assertThat(lines.get(3)[1], is(name_3));
        assertThat(lines.get(3)[2], is("1"));

        assertThat(lines.get(4)[0], is("2019-04-08"));
        assertThat(lines.get(4)[1], is(name_2));
        assertThat(lines.get(4)[2], is("1"));

        assertThat(lines.get(5)[0], is("2019-04-08"));
        assertThat(lines.get(5)[1], is(name_3));
        assertThat(lines.get(5)[2], is("1"));

        assertThat(lines.get(6)[0], is("Total Messages"));
        assertThat(lines.get(6)[1], is(""));
        assertThat(lines.get(6)[2], is("6"));
    }
}