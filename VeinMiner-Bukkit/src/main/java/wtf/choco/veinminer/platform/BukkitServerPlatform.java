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
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.config.BukkitVeinMinerConfiguration;
import wtf.choco.veinminer.config.VeinMinerConfiguration;
import wtf.choco.veinminer.platform.world.BlockState;
import wtf.choco.veinminer.platform.world.BlockType;
import wtf.choco.veinminer.platform.world.BukkitBlockState;
import wtf.choco.veinminer.platform.world.BukkitBlockType;
import wtf.choco.veinminer.platform.world.BukkitItemType;
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
    private final ServerCommandRegistry commandRegistry;
    private final UpdateChecker updateChecker;

    private final VeinMinerDetails details;

    private BukkitServerPlatform(VeinMinerPlugin plugin) {
        this.plugin = plugin;
        this.config = new BukkitVeinMinerConfiguration(plugin);
        this.eventDispatcher = new BukkitServerEventDispatcher();
        this.updateChecker = new SpigotMCUpdateChecker(plugin, 12038);
        this.commandRegistry = new BukkitServerCommandRegistry(plugin);

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
            return BukkitBlockState.of(Bukkit.createBlockData(state));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Nullable
    @Override
    public BlockType getBlockType(@NotNull String type) {
        Material material = Material.matchMaterial(type);
        return (material != null && material.isBlock()) ? BukkitBlockType.of(material) : null;
    }

    @Nullable
    @Override
    public ItemType getItemType(@NotNull String type) {
        Material material = Material.matchMaterial(type);
        return (material != null && material.isItem()) ? BukkitItemType.of(material) : null;
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

    @NotNull
    @Override
    public ServerCommandRegistry getCommandRegistry() {
        return commandRegistry;
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

    public static BukkitServerPlatform getInstance() {
        return (instance != null) ? instance : (instance = new BukkitServerPlatform(VeinMinerPlugin.getInstance()));
    }

    public static <C extends Collection<ItemType>> C toItemType(Collection<Material> from, Supplier<C> collectionCreator) {
        C collection = collectionCreator.get();

        for (Material material : from) {
            collection.add(BukkitItemType.of(material));
        }

        return collection;
    }

}
