package uk.gov.justice.artemis.manager.connector.filehandling;

import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;
import static org.apache.commons.io.FileUtils.getFile;
import static org.apache.commons.io.FileUtils.readFileToString;

import java.io.File;

public class TextMessageFileContentsReader {

    public String readContentsOf(final String pathToFile) {

        try {
            final File file = getFile(pathToFile);

            if(! file.exists()) {
                throw new MessageFileException(format("Failed to find file '%s'", file.getAbsolutePath()));
            }

            return readFileToString(file, defaultCharset());
        } catch (final Exception e) {
            throw new MessageFileException(format("Failed to read file '%s'", pathToFile), e);
        }
    }
}
