package wtf.choco.veinminer.platform.world;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

/**
 * A Bukkit implementation of {@link BlockState}.
 */
public final class BukkitBlockState implements BlockState {

    private static final Map<BlockData, BlockState> CACHE = new HashMap<>();

    private final BlockData blockData;

    private BukkitBlockState(@NotNull BlockData blockData) {
        this.blockData = blockData;
    }

    @NotNull
    @Override
    public BlockType getType() {
        return BukkitBlockType.of(blockData.getBlockType());
    }

    @NotNull
    @Override
    public String getAsString(boolean hideUnspecified) {
        return blockData.getAsString(hideUnspecified);
    }

    @Override
    public boolean matches(@NotNull BlockState state) {
        return (state instanceof BukkitBlockState other) && blockData.matches(other.blockData);
    }

    /**
     * Get a {@link BlockState} for the given {@link BlockData}.
     *
     * @param blockData the block data
     *
     * @return the BlockState
     */
    @NotNull
    public static BlockState of(@NotNull BlockData blockData) {
        return CACHE.computeIfAbsent(blockData, BukkitBlockState::new);
    }

    @Override
    public int hashCode() {
        return blockData.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof BukkitBlockState other && Objects.equals(blockData, other.blockData));
    }

    @Override
    public String toString() {
        return blockData.toString();
    }

}
