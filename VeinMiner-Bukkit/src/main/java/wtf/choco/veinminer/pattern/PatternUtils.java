package wtf.choco.veinminer.pattern;

import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.api.VBlockFace;
import wtf.choco.veinminer.data.AlgorithmConfig;
import wtf.choco.veinminer.data.MaterialAlias;
import wtf.choco.veinminer.data.block.VeinBlock;

/**
 * Utility methods for {@link VeinMiningPattern} implementations.
 *
 * @author Parker Hawke - 2008Choco
 */
public final class PatternUtils {

    private static final VBlockFace[] LIMITED_FACES = {
            VBlockFace.UP, VBlockFace.DOWN, VBlockFace.NORTH, VBlockFace.SOUTH, VBlockFace.EAST,
            VBlockFace.WEST, VBlockFace.NORTH_EAST, VBlockFace.NORTH_WEST, VBlockFace.SOUTH_EAST,
            VBlockFace.SOUTH_WEST
    };

    private PatternUtils() { }

    /**
     * Check if a block is encapsulated by the VeinBlock type or considered aliased under
     * the provided alias (if present).
     *
     * @param type the type for which to check
     * @param origin the origin type
     * @param alias the alias. null if no alias
     * @param block the block to validate
     *
     * @return true if the provided block is of that type or aliased, false otherwise
     */
    public static boolean isOfType(@NotNull VeinBlock type, @NotNull Block origin, @Nullable MaterialAlias alias, @NotNull Block block) {
        if (type.isWildcard()) {
            return origin.getType() == block.getType();
        }

        return type.encapsulates(block) || (alias != null && alias.isAliased(block));
    }

    /**
     * Check if a block is encapsulated by the VeinBlock type or considered aliased under
     * the provided alias (if present).
     *
     * @param type the type for which to check
     * @param alias the alias. null if no alias
     * @param block the block to validate
     *
     * @return true if the provided block is of that type or aliased, false otherwise
     *
     * @deprecated see {@link #isOfType(VeinBlock, Block, MaterialAlias, Block)}
     */
    @Deprecated
    public static boolean isOfType(@NotNull VeinBlock type, @Nullable MaterialAlias alias, @NotNull Block block) {
        return type.encapsulates(block) || (alias != null && alias.isAliased(block));
    }

    /**
     * Get an array of VBlockFaces to mine based on VeinMiner's "IncludeEdges" configuration.
     *
     * @param algorithmConfig the algorithm configuration
     *
     * @return the block face array
     */
    @NotNull
    public static VBlockFace[] getFacesToMine(@NotNull AlgorithmConfig algorithmConfig) {
        return algorithmConfig.includesEdges() ? VBlockFace.values() : LIMITED_FACES;
    }

}
