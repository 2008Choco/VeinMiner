package wtf.choco.veinminer.platform;

import com.google.common.collect.Collections2;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.command.Command;
import wtf.choco.veinminer.config.BukkitVeinMinerConfiguration;
import wtf.choco.veinminer.config.VeinMinerConfiguration;
import wtf.choco.veinminer.platform.world.BlockState;
import wtf.choco.veinminer.platform.world.BlockType;
import wtf.choco.veinminer.platform.world.ItemType;
import wtf.choco.veinminer.update.SpigotMCUpdateChecker;
import wtf.choco.veinminer.update.UpdateChecker;

/**
 * A Bukkit implementation of {@link ServerPlatform}.
 */
public final class BukkitServerPlatform implements ServerPlatform {

    private static BukkitServerPlatform instance;

    private final Map<UUID, PlatformPlayer> platformPlayers = new HashMap<>();

    private final VeinMinerPlugin plugin;
    private final VeinMinerConfiguration config;
    private final ServerEventDispatcher eventDispatcher;
    private final UpdateChecker updateChecker;

    private final VeinMinerDetails details;

    private BukkitServerPlatform(VeinMinerPlugin plugin) {
        this.plugin = plugin;
        this.config = new BukkitVeinMinerConfiguration(plugin);
        this.eventDispatcher = new BukkitServerEventDispatcher();
        this.updateChecker = new SpigotMCUpdateChecker(plugin, 12038);

        PluginDescriptionFile description = plugin.getDescription();
        this.details = new VeinMinerDetails(description.getName(), description.getVersion(), Collections.unmodifiableList(description.getAuthors()), description.getWebsite());
    }

    @NotNull
    @Override
    public VeinMinerDetails getVeinMinerDetails() {
        return details;
    }

    @NotNull
    @Override
    public File getVeinMinerPluginDirectory() {
        return plugin.getDataFolder();
    }

    @NotNull
    @Override
    public Logger getLogger() {
        return plugin.getLogger();
    }

    @NotNull
    @Override
    public VeinMinerConfiguration getConfig() {
        return config;
    }

    @Nullable
    @Override
    public BlockState getState(@NotNull String state) {
        try {
            return BukkitAdapter.adapt(Bukkit.createBlockData(state));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Nullable
    @Override
    public BlockType getBlockType(@NotNull String type) {
        Material material = Material.matchMaterial(type);
        return (material != null && material.isBlock()) ? BukkitAdapter.adaptBlock(material) : null;
    }

    @Nullable
    @Override
    public ItemType getItemType(@NotNull String type) {
        Material material = Material.matchMaterial(type);
        return (material != null && material.isItem()) ? BukkitAdapter.adaptItem(material) : null;
    }

    @NotNull
    @Override
    public List<String> getAllBlockTypeKeys() {
        return Arrays.stream(Material.values()).filter(Material::isBlock).map(material -> material.getKey().toString()).toList();
    }

    @NotNull
    @Override
    public List<String> getAllItemTypeKeys() {
        return Arrays.stream(Material.values()).filter(Material::isItem).map(material -> material.getKey().toString()).toList();
    }

    @NotNull
    @Override
    public PlatformPlayer getPlatformPlayer(@NotNull UUID playerUUID) {
        return platformPlayers.computeIfAbsent(playerUUID, BukkitPlatformPlayer::new);
    }

    @NotNull
    @Override
    public Collection<? extends PlatformPlayer> getOnlinePlayers() {
        return Collections2.transform(Bukkit.getOnlinePlayers(), player -> getPlatformPlayer(player.getUniqueId()));
    }

    @NotNull
    @Override
    public PlatformPermission getOrRegisterPermission(String permission, Supplier<String> description, PlatformPermission.Default permissionDefault) {
        PluginManager pluginManager = Bukkit.getPluginManager();
        Permission bukkitPermission = pluginManager.getPermission(permission);

        // If the permission does not exist, we'll create and register it
        if (bukkitPermission == null) {
            bukkitPermission = new Permission(permission, description.get(), PermissionDefault.getByName(permissionDefault.name()));
            pluginManager.addPermission(bukkitPermission);
        }

        return new BukkitPlatformPermission(bukkitPermission);
    }

    @NotNull
    @Override
    public ServerEventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }

    @Override
    public void registerCommand(@NotNull String name, @NotNull Command command) {
        PluginCommand pluginCommand = plugin.getCommand(name);

        if (pluginCommand == null) {
            return;
        }

        pluginCommand.setExecutor((sender, bukkitCommand, label, args) -> command.execute(wrap(sender), label, args));
        pluginCommand.setTabCompleter((sender, bukkitCommand, alias, args) -> command.tabComplete(wrap(sender), alias, args));
    }

    private PlatformCommandSender wrap(CommandSender sender) {
        return (sender instanceof Player player) ? BukkitAdapter.adapt(player) : new BukkitPlatformCommandSender(sender);
    }

    @NotNull
    @Override
    public UpdateChecker getUpdateChecker() {
        return updateChecker;
    }

    @Override
    public void runTaskLater(@NotNull Runnable runnable, int ticks) {
        Bukkit.getScheduler().runTaskLater(plugin, runnable, ticks);
    }

    @Override
    public void runTaskAsynchronously(@NotNull Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    /**
     * Get the {@link BukkitServerPlatform} singleton instance.
     *
     * @return the instance
     */
    public static BukkitServerPlatform getInstance() {
        return (instance != null) ? instance : (instance = new BukkitServerPlatform(VeinMinerPlugin.getInstance()));
    }

}
