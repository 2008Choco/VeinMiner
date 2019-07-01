package wtf.choco.veinminer.pattern;

import java.util.Set;

import com.google.common.base.Preconditions;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.data.AlgorithmConfig;
import wtf.choco.veinminer.data.MaterialAlias;
import wtf.choco.veinminer.data.block.VeinBlock;
import wtf.choco.veinminer.tool.ToolCategory;
import wtf.choco.veinminer.tool.ToolTemplate;

/**
 * Represents a mining algorithm capable of allocating which blocks should be broken by VeinMiner
 * when a successful vein mine occurs. It is recommended that implementations of VeinMiningPattern
 * are singleton instances, although this is not a requirement.
 */
public interface VeinMiningPattern extends Keyed, BlockAllocator {

    /**
     * Allocate the blocks that should be broken by the vein mining pattern. Note that the breaking
     * of the blocks should not be handled by the pattern, but rather the plugin itself. This method
     * serves primarily to search for valid blocks to break in a vein.
     * <p>
     * <b>NOTE:</b> If null is added to the "blocks" set, a NullPointerException will be thrown and
     * the method will fail.
     *
     * @param blocks a set of all blocks to break. Valid blocks should be added here. The "origin"
     * block passed to this method will be added automatically
     * @param type the type of VeinBlock being vein mined
     * @param origin the block where the vein mine was initiated
     * @param category the tool category used to break the block
     * @param template the tool template used to break the block. May be null
     * @param algorithmConfig the algorithm configuration
     */
    public default void allocateBlocks(@NotNull Set<Block> blocks, @NotNull VeinBlock type, @NotNull Block origin, @NotNull ToolCategory category, @Nullable ToolTemplate template, @NotNull AlgorithmConfig algorithmConfig) {
        this.allocateBlocks(blocks, type, origin, category, template, algorithmConfig, null);
    }

    /**
     * Create a new VeinMiningPattern using a custom {@link BlockAllocator}.
     *
     * @param key the key of the vein mining pattern. Must be unique and not null
     * @param blockAllocator the allocator to compute breakable blocks. Must not be null
     *
     * @return the resulting VeinMiningPattern instance
     */
    @NotNull
    public static VeinMiningPattern createNewPattern(@NotNull NamespacedKey key, @NotNull BlockAllocator blockAllocator) {
        Preconditions.checkNotNull(key, "Pattern must not have a null key");
        Preconditions.checkNotNull(blockAllocator, "Block computer must not be null");

        if (blockAllocator instanceof VeinMiningPattern) {
            return (VeinMiningPattern) blockAllocator;
        }

        return new VeinMiningPattern() {
            @Override
            public void allocateBlocks(Set<Block> blocks, VeinBlock type, Block origin, ToolCategory category, ToolTemplate template, AlgorithmConfig algorithmConfig, MaterialAlias alias) {
                blockAllocator.allocateBlocks(blocks, type, origin, category, template, algorithmConfig, alias);
            }

            @Override
            public NamespacedKey getKey() {
                return key;
            }
        };
    }

}
