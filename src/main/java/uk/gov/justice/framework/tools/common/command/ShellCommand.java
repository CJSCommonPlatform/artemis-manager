package uk.gov.justice.framework.tools.common.command;

/**
 * Implement this interface to have the reflection utils pick
 * up the class at runtime and add to JCommander instance in order
 * to setup a CMD command.
 */
public interface ShellCommand {

    void run(final String[] args);

}
