package uk.gov.justice.framework.tools.common.command;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.junit.Test;

public class BootstrapTest {

    @Test
    public void shouldBootstrapShellCommand() throws Exception {
        final String[] inputArgs = new String[]{"testcommand", "-s", "testValue"};

        Bootstrap.main(inputArgs);

        assertThat(TestCommand.args, is(inputArgs));
        assertThat(TestCommand.value, is("testValue"));
    }

    @Test
    public void shouldNotBootstrapShellCommandThatIsAbstract() throws Exception {
        final String[] inputArgs = new String[]{"testcommand", "-s", "testValue"};

        Bootstrap.main(inputArgs);

        assertThat(TestAbstractCommand.args, nullValue());
        assertThat(TestAbstractCommand.value, nullValue());
    }

    @Parameters(separators = "=", commandDescription = "Test Command")
    public static class TestCommand implements ShellCommand {

        private static String[] args;

        @Parameter(names = "-s", description = "test value")
        private static String value;

        @Override
        public void run(final String[] args) {
            TestCommand.args = args;
        }
    }

    @Parameters(separators = "=", commandDescription = "Test Command")
    public static abstract class TestAbstractCommand implements ShellCommand {

        private static String[] args;

        @Parameter(names = "-s", description = "test value")
        private static String value;

        @Override
        public void run(final String[] args) {
            TestCommand.args = args;
        }
    }


}