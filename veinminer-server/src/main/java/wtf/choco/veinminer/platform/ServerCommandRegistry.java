package wtf.choco.veinminer.platform;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.command.CommandExecutor;

/**
 * The server's command registry.
 */
public interface ServerCommandRegistry {

    /**
     * Register a command with the given name to the provided {@link CommandExecutor}.
     *
     * @param name the name of the command to register
     * @param command the executable to call when the command is run
     */
    public void registerCommand(@NotNull String name, @NotNull CommandExecutor command);

}
