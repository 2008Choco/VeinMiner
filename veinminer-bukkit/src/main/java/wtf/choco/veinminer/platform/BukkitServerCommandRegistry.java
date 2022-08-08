package wtf.choco.veinminer.platform;

import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.command.CommandExecutor;

/**
 * Bukkit implementation of {@link ServerCommandRegistry}.
 */
public final class BukkitServerCommandRegistry implements ServerCommandRegistry {

    private final JavaPlugin plugin;

    /**
     * Construct a new {@link BukkitServerCommandRegistry}.
     *
     * @param plugin the plugin instance
     */
    public BukkitServerCommandRegistry(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void registerCommand(@NotNull String name, @NotNull CommandExecutor command) {
        PluginCommand pluginCommand = plugin.getCommand(name);

        if (pluginCommand == null) {
            return;
        }

        pluginCommand.setExecutor((sender, bukkitCommand, label, args) -> command.execute(wrap(sender), label, args));
        pluginCommand.setTabCompleter((sender, bukkitCommand, alias, args) -> command.tabComplete(wrap(sender), alias, args));
    }

    private PlatformCommandSender wrap(CommandSender sender) {
        if (sender instanceof Player player) {
            return BukkitServerPlatform.getInstance().getPlatformPlayer(player.getUniqueId());
        }

        return new BukkitPlatformCommandSender(sender);
    }

}
