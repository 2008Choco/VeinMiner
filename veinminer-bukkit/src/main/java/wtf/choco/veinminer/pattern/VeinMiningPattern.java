package wtf.choco.veinminer.pattern;

import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.network.data.NamespacedKey;
import wtf.choco.veinminer.block.BlockList;
import wtf.choco.veinminer.block.VeinMinerBlock;
import wtf.choco.veinminer.config.VeinMiningConfiguration;
import wtf.choco.veinminer.util.BlockPosition;

/**
 * Represents a pattern used to allocate blocks for vein mining.
 */
public interface VeinMiningPattern {

    /**
     * Get the {@link NamespacedKey} of this pattern.
     *
     * @return the key
     */
    @NotNull
    public NamespacedKey getKey();

    /**
     * Allocate all {@link BlockPosition BlockPositions} that should be mined according to the input values.
     *
     * @param origin the position at which the vein mining was initiated
     * @param destroyedFace the face on which the block was destroyed
     * @param block the type of {@link VeinMinerBlock} that was broken at the origin
     * @param config the configuration applicable for this instance of vein mining
     * @param aliasList a {@link BlockList} of all blocks that should also be considered. May be empty
     *
     * @return the allocated block positions
     */
    @NotNull
    public List<Block> allocateBlocks(@NotNull Block origin, @NotNull BlockFace destroyedFace, @NotNull VeinMinerBlock block, @NotNull VeinMiningConfiguration config, @Nullable BlockList aliasList);

    /**
     * Allocate all {@link BlockPosition BlockPositions} that should be mined according to the input values.
     *
     * @param origin the position at which the vein mining was initiated
     * @param destroyedFace the face on which the block was destroyed
     * @param block the type of {@link VeinMinerBlock} that was broken at the origin
     * @param config the configuration applicable for this instance of vein mining
     *
     * @return the allocated block positions
     */
    @NotNull
    public default List<Block> allocateBlocks(@NotNull Block origin, @NotNull BlockFace destroyedFace, @NotNull VeinMinerBlock block, @NotNull VeinMiningConfiguration config) {
        return allocateBlocks(origin, destroyedFace, block, config, null);
    }

    /**
     * Get the permission node required to use this pattern.
     *
     * @return the permission node, or null if none
     */
    @Nullable
    public default String getPermission() {
        return null;
    }

}
