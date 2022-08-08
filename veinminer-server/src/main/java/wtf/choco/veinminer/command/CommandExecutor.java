package wtf.choco.veinminer.command;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.platform.PlatformCommandSender;

/**
 * Represents a command that can be executed and provide tab completion.
 */
public interface CommandExecutor {

    /**
     * Execute this command.
     *
     * @param sender the sender that performed the command
     * @param label the command label
     * @param args the supplied arguments
     *
     * @return true if successful, false otherwise
     */
    public boolean execute(@NotNull PlatformCommandSender sender, @NotNull String label, String @NotNull [] args);

    /**
     * Provide tab completion for this command.
     *
     * @param sender the sender that is tab completing this command.
     * @param label the command label
     * @param args the current supplied args
     *
     * @return the tab complete suggestions, or null to tab complete player names
     */
    @Nullable
    public List<String> tabComplete(@NotNull PlatformCommandSender sender, @NotNull String label, String @NotNull [] args);

}
