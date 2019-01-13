package wtf.choco.veinminer.pattern;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;

import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.api.VBlockFace;
import wtf.choco.veinminer.data.MaterialAlias;
import wtf.choco.veinminer.data.block.VeinBlock;
import wtf.choco.veinminer.tool.ToolCategory;

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

	private static final VBlockFace[] LIMITED_FACES = {
			VBlockFace.UP, VBlockFace.DOWN, VBlockFace.NORTH, VBlockFace.SOUTH, VBlockFace.EAST,
			VBlockFace.WEST, VBlockFace.NORTH_EAST, VBlockFace.NORTH_WEST, VBlockFace.SOUTH_EAST,
			VBlockFace.SOUTH_WEST
	};

	private static PatternThorough instance;

	private final VeinMiner plugin;
	private final NamespacedKey key;
	private final List<Block> blockBuffer = new ArrayList<>();

	private PatternThorough() {
		this.plugin = VeinMiner.getPlugin();
		this.key = new NamespacedKey(plugin, "thorough");
	}

	@Override
	public void allocateBlocks(Set<Block> blocks, VeinBlock type, Block origin, ToolCategory category, MaterialAlias alias) {
		int maxVeinSize = category.getMaxVeinSize();
		VBlockFace[] facesToMine = getFacesToMine();

		while (blocks.size() <= maxVeinSize) {
			Iterator<Block> trackedBlocks = blocks.iterator();
			while (trackedBlocks.hasNext() && blocks.size() + blockBuffer.size() <= maxVeinSize) {
				Block b = trackedBlocks.next();
				for (VBlockFace face : facesToMine) {
					if (blocks.size() + blockBuffer.size() >= maxVeinSize) {
						break;
					}

					Block nextBlock = face.getRelative(b);
					if (blocks.contains(nextBlock) || !blockIsSameMaterial(type, nextBlock, alias)) {
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

	@Override
	public NamespacedKey getKey() {
		return key;
	}

	private boolean blockIsSameMaterial(VeinBlock type, Block block, MaterialAlias alias) {
		return type.encapsulates(block) || (alias != null && alias.isAliased(block));
	}

	private VBlockFace[] getFacesToMine() {
		return plugin.getConfig().getBoolean("IncludeEdges") ? VBlockFace.values() : LIMITED_FACES;
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