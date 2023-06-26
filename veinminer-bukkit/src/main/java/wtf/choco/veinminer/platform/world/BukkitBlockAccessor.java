package wtf.choco.veinminer.platform.world;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.platform.BukkitAdapter;

/**
 * Implementation of {@link BlockAccessor} for Bukkit {@link World Worlds}.
 */
public final class BukkitBlockAccessor implements BlockAccessor {

    private static final Map<UUID, BukkitBlockAccessor> BLOCK_ACCESSORS = new HashMap<>();

    private final Reference<World> world;

    private BukkitBlockAccessor(@NotNull World world) {
        this.world = new WeakReference<>(world);
    }

    @Nullable
    public World getWorld() {
        return world.get();
    }

    @NotNull
    public World getWorldOrThrow() {
        World world = this.world.get();

        if (world == null) {
            throw new IllegalStateException("world is null");
        }

        return world;
    }

    @NotNull
    @Override
    public String getWorldName() {
        return world.get().getName();
    }

    @NotNull
    @Override
    public BlockType getType(int x, int y, int z) {
        return BukkitAdapter.adaptBlock(world.get().getBlockAt(x, y, z).getType());
    }

    @NotNull
    @Override
    public BlockState getState(int x, int y, int z) {
        return BukkitAdapter.adapt(world.get().getBlockData(x, y, z));
    }

    /**
     * Get a {@link BukkitBlockAccessor} for the given {@link World}.
     *
     * @param world the world for which to get a block accessor
     *
     * @return the block accessor
     */
    @NotNull
    public static BukkitBlockAccessor forWorld(@NotNull World world) {
        return BLOCK_ACCESSORS.computeIfAbsent(world.getUID(), uuid -> new BukkitBlockAccessor(world));
    }

}
