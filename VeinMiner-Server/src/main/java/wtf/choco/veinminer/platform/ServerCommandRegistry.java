package wtf.choco.veinminer.platform;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.command.CommandExecutor;

public interface ServerCommandRegistry {

    public void registerCommand(@NotNull String name, @NotNull CommandExecutor command);

}
