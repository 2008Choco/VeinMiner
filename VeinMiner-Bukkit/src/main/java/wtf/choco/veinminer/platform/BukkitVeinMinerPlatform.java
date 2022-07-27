package wtf.choco.veinminer.platform;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.platform.world.BlockState;
import wtf.choco.veinminer.platform.world.BlockType;
import wtf.choco.veinminer.platform.world.BukkitBlockState;
import wtf.choco.veinminer.platform.world.BukkitBlockType;
import wtf.choco.veinminer.platform.world.BukkitItemType;
import wtf.choco.veinminer.platform.world.ItemType;

/**
 * A Bukkit implementation of {@link VeinMinerPlatform}.
 */
public final class BukkitVeinMinerPlatform implements VeinMinerPlatform {

    private static VeinMinerPlatform instance;

    private final Map<UUID, PlatformPlayer> platformPlayers = new HashMap<>();

    private final VeinMinerPlugin plugin;
    private final VeinMinerEventDispatcher eventDispatcher;

    private BukkitVeinMinerPlatform(VeinMinerPlugin plugin) {
        this.plugin = plugin;
        this.eventDispatcher = new BukkitVeinMinerEventDispatcher();
    }

    @NotNull
    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
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
        return (material != null) ? BukkitBlockType.of(material) : null;
    }

    @Nullable
    @Override
    public ItemType getItemType(@NotNull String type) {
        Material material = Material.matchMaterial(type);
        return (material != null) ? BukkitItemType.of(material) : null;
    }

    @NotNull
    @Override
    public PlatformPlayer getPlatformPlayer(@NotNull UUID playerUUID) {
        return platformPlayers.computeIfAbsent(playerUUID, BukkitPlatformPlayer::new);
    }

    @NotNull
    @Override
    public VeinMinerEventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }

    @Override
    public void runTaskLater(@NotNull Runnable runnable, int ticks) {
        Bukkit.getScheduler().runTaskLater(plugin, runnable, ticks);
    }

    public static VeinMinerPlatform getInstance() {
        return (instance != null) ? instance : (instance = new BukkitVeinMinerPlatform(VeinMinerPlugin.getInstance()));
    }

    public static <C extends Collection<ItemType>> C toItemType(Collection<Material> from, Supplier<C> collectionCreator) {
        C collection = collectionCreator.get();

        for (Material material : from) {
            collection.add(BukkitItemType.of(material));
        }

        return collection;
    }

}
