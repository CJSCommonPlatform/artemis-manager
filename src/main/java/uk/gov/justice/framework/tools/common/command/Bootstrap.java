package uk.gov.justice.framework.tools.common.command;

import static java.lang.Class.forName;
import static java.lang.reflect.Modifier.isAbstract;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Optional;
import java.util.Set;

import org.reflections.Reflections;
import org.slf4j.Logger;

import com.beust.jcommander.JCommander;

public class Bootstrap {

    private static final Logger LOGGER = getLogger(Bootstrap.class);
    private final JCommander commander;

    private Bootstrap() {
        this.commander = new JCommander();
        this.commander.setAcceptUnknownOptions(true);
        this.commander.setAllowAbbreviatedOptions(true);
    }

    public static void main(final String... args) {
        new Bootstrap().setup(args);
    }

    private void setup(final String[] args) {
        final Reflections reflections = new Reflections("uk.gov.justice.framework.tools");
        final Set<Class<? extends ShellCommand>> subTypes = reflections.getSubTypesOf(ShellCommand.class);

        subTypes.stream()
                .filter(this::commandClassIsNotAbstract)
                .forEach(this::createInstanceAndAddToJCommander);

        commander.parse(args);

        getParsedShellCommand().ifPresent(command -> command.run(args));
    }

    private Optional<ShellCommand> getParsedShellCommand() {
        return commander
                .getCommands()
                .get(commander.getParsedCommand())
                .getObjects().stream()
                .findFirst()
                .map(command -> ((ShellCommand) command));
    }

    private boolean commandClassIsNotAbstract(final Class<? extends ShellCommand> commandClass) {
        return !isAbstract(commandClass.getModifiers());
    }

    private void createInstanceAndAddToJCommander(final Class<? extends ShellCommand> commandClass) {
        try {
            commander.addCommand(commandClass.getSimpleName().toLowerCase(), forName(commandClass.getName()).newInstance());
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            LOGGER.error("Unable to create instance of {}", commandClass.getName());
        }
    }
}