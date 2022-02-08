package wtf.choco.veinminer.block;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.platform.BlockState;
import wtf.choco.veinminer.platform.BlockType;
import wtf.choco.veinminer.platform.BukkitBlockState;
import wtf.choco.veinminer.platform.BukkitBlockType;

/**
 * Implementation of {@link BlockAccessor} for Bukkit {@link World Worlds}.
 */
public final class BukkitBlockAccessor implements BlockAccessor {

    private static final Map<UUID, BlockAccessor> BLOCK_ACCESSORS = new HashMap<>();

    private final Reference<World> world;

    private BukkitBlockAccessor(@NotNull World world) {
        this.world = new WeakReference<>(world);
    }

    @NotNull
    @Override
    public String getWorldName() {
        return world.get().getName();
    }

    @NotNull
    @Override
    public BlockType getType(int x, int y, int z) {
        return BukkitBlockType.of(world.get().getBlockAt(x, y, z).getType());
    }

    @NotNull
    @Override
    public BlockState getState(int x, int y, int z) {
        return BukkitBlockState.of(world.get().getBlockData(x, y, z));
    }

    /**
     * Get a {@link BlockAccessor} for the given {@link World}.
     *
     * @param world the world for which to get a block accessor
     *
     * @return the block accessor
     */
    @NotNull
    public static BlockAccessor forWorld(@NotNull World world) {
        return BLOCK_ACCESSORS.computeIfAbsent(world.getUID(), uuid -> new BukkitBlockAccessor(world));
    }

}
