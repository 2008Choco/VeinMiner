package wtf.choco.veinminer.pattern;

import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.block.BlockList;
import wtf.choco.veinminer.block.VeinMinerBlock;
import wtf.choco.veinminer.block.VeinMinerBlockTag;
import wtf.choco.veinminer.block.VeinMinerBlockWildcard;

/**
 * Utility methods for implementing {@link VeinMiningPattern VeinMiningPatterns}.
 */
public final class PatternUtils {

    private PatternUtils() { }

    /**
     * Check whether or not the given {@link BlockData} matches either the {@link VeinMinerBlock} or is
     * present in the given {@link BlockList}.
     *
     * @param block the block against which to check the state
     * @param aliasList the alias list, or null if no aliases
     * @param origin the origin block that was broken
     * @param current the current block state to check
     *
     * @return true if {@code current} either {@link VeinMinerBlock#matchesState(BlockData) matches state}
     * with the {@code block} or is {@link BlockList#containsState(BlockData) contained in the alias list},
     * false otherwise
     */
    public static boolean typeMatches(@NotNull VeinMinerBlock block, @Nullable BlockList aliasList, @NotNull BlockData origin, @NotNull BlockData current) {
        if (block instanceof VeinMinerBlockWildcard || block instanceof VeinMinerBlockTag) {
            return origin.getMaterial() == current.getMaterial() || (aliasList != null && aliasList.containsState(current));
        }

        return block.matchesState(current) || (aliasList != null && aliasList.containsState(current));
    }

}
