package wtf.choco.veinminer.pattern;

import java.util.List;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.block.BlockList;
import wtf.choco.veinminer.block.VeinMinerBlock;
import wtf.choco.veinminer.config.VeinMiningConfiguration;

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
     * Allocate a {@link List} of {@link Block Blocks} that should be destroyed. The returned List of
     * blocks will be broken in the order that they are inserted.
     *
     * @param origin the block where vein miner was initiated
     * @param destroyedFace the block face that was destroyed
     * @param block the {@link VeinMinerBlock} that was broken at the origin
     * @param config the configuration applicable for this vein mine
     * @param aliasList a {@link BlockList} of all blocks that should also be considered. May be empty
     * or null
     *
     * @return the allocated blocks to break
     *
     * @apiNote mutability of the returned List cannot be guaranteed. Pattern implementations may or
     * may not return immutable lists, therefore it's best to assume that it will be immutable
     */
    @NotNull
    public List<Block> allocateBlocks(@NotNull Block origin, @NotNull BlockFace destroyedFace, @NotNull VeinMinerBlock block, @NotNull VeinMiningConfiguration config, @Nullable BlockList aliasList);

    /**
     * Allocate a {@link List} of {@link Block Blocks} that should be destroyed. The returned List of
     * blocks will be broken in the order that they are inserted.
     *
     * @param origin the block where vein miner was initiated
     * @param destroyedFace the block face that was destroyed
     * @param block the {@link VeinMinerBlock} that was broken at the origin
     * @param config the configuration applicable for this vein mine
     *
     * @return the allocated blocks to break
     *
     * @apiNote mutability of the returned List cannot be guaranteed. Pattern implementations may or
     * may not return immutable lists, therefore it's best to assume that it will be immutable
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
