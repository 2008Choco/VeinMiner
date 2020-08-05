package wtf.choco.veinminer.pattern;

import java.util.Set;

import org.bukkit.Keyed;
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
public interface VeinMiningPattern extends Keyed {

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
     * @param alias an alias of the block being broken if one exists. May be null
     */
    public void allocateBlocks(@NotNull Set<Block> blocks, @NotNull VeinBlock type, @NotNull Block origin, @NotNull ToolCategory category, @Nullable ToolTemplate template, @NotNull AlgorithmConfig algorithmConfig, @Nullable MaterialAlias alias);

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

}
