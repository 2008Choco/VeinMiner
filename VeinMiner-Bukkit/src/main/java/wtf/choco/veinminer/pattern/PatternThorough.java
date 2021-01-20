package wtf.choco.veinminer.pattern;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.api.VBlockFace;
import wtf.choco.veinminer.data.AlgorithmConfig;
import wtf.choco.veinminer.data.MaterialAlias;
import wtf.choco.veinminer.data.block.VeinBlock;
import wtf.choco.veinminer.tool.ToolCategory;
import wtf.choco.veinminer.tool.ToolTemplate;

/**
 * A {@link VeinMiningPattern} implementation that "pulsates" from the origin outwards. Every
 * iteration, every block starting from the origin will be checks for adjacent blocks.
 * <p>
 * This pattern is less efficient than {@link PatternExpansive} when used for larger veins, but
 * may be more performant when dealing with smaller veins.
 * <p>
 * This pattern should be considered as effectively deprecated. While not literally deprecated,
 * the expansive pattern should be used in place of this as it will yield results more quickly
 * and in an efficient manner.
 *
 * @author Parker Hawke - 2008Choco
 */
public final class PatternThorough implements VeinMiningPattern {

    private static PatternThorough instance;

    private final NamespacedKey key;
    private final List<Block> blockBuffer = new ArrayList<>();

    private PatternThorough() {
        this.key = new NamespacedKey(VeinMiner.getPlugin(), "thorough");
    }

    @Override
    public void allocateBlocks(@NotNull Set<Block> blocks, @NotNull VeinBlock type, @NotNull Block origin, @NotNull ToolCategory category, @Nullable ToolTemplate template, @NotNull AlgorithmConfig algorithmConfig, @Nullable MaterialAlias alias) {
        int maxVeinSize = algorithmConfig.getMaxVeinSize();
        VBlockFace[] facesToMine = PatternUtils.getFacesToMine(algorithmConfig);

        while (blocks.size() < maxVeinSize) {
            Iterator<Block> trackedBlocks = blocks.iterator();
            while (trackedBlocks.hasNext() && blocks.size() + blockBuffer.size() <= maxVeinSize) {
                Block current = trackedBlocks.next();
                for (VBlockFace face : facesToMine) {
                    if (blocks.size() + blockBuffer.size() >= maxVeinSize) {
                        break;
                    }

                    Block nextBlock = face.getRelative(current);
                    if (blocks.contains(nextBlock) || !PatternUtils.isOfType(type, alias, nextBlock)) {
                        continue;
                    }

                    this.blockBuffer.add(nextBlock);
                }
            }

            if (blockBuffer.size() == 0) {
                break;
            }

            blocks.addAll(blockBuffer);
            this.blockBuffer.clear();
        }
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return key;
    }

    /**
     * Get a singleton instance of the default pattern.
     *
     * @return the default pattern
     */
    public static PatternThorough get() {
        return (instance == null) ? instance = new PatternThorough() : instance;
    }

}
