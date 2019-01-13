package wtf.choco.veinminer.pattern;

import java.util.ArrayList;
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
 * A {@link VeinMiningPattern} implementation that "expands" to search for similar blocks. Using the
 * outermost layer of blocks in the block list, the search propagates outwards rather than iterating over
 * blocks that have already been identified as surrounded by non-veinmineable (or already allocated)
 * blocks.
 * <p>
 * This implementation is substantially more efficient for larger veins of ores when compared to
 * {@link PatternThorough} though may be overkill for smaller veins of ores. This pattern is still
 * recommended over the aforementioned pattern, however, and as such is used as the default.
 *
 * @author Parker Hawke - 2008Choco
 */
public final class PatternExpansive implements VeinMiningPattern {

	private static PatternExpansive instance;

	private final List<Block> buffer = new ArrayList<>(32), recent = new ArrayList<>(32);
	private final NamespacedKey key;

	private PatternExpansive() {
		this.key = new NamespacedKey(VeinMiner.getPlugin(), "expansive");
	}

	@Override
	public void allocateBlocks(Set<Block> blocks, VeinBlock type, Block origin, ToolCategory category, MaterialAlias alias) {
		this.recent.add(origin); // For first iteration

		int maxVeinSize = category.getMaxVeinSize();
		VBlockFace[] facesToMine = PatternUtils.getFacesToMine();

		while (blocks.size() <= maxVeinSize) {
			recentSearch:
			for (Block current : recent) {
				for (VBlockFace face : facesToMine) {
					Block relative = face.getRelative(current);
					if (blocks.contains(relative) || !PatternUtils.isOfType(type, alias, relative)) {
						continue;
					}

					if (blocks.size() + buffer.size() > maxVeinSize) {
						break recentSearch;
					}

					this.buffer.add(relative);
				}
			}

			if (buffer.size() == 0) { // No more blocks to allocate :D
				break;
			}

			this.recent.clear();
			this.recent.addAll(buffer);
			blocks.addAll(buffer);

			this.buffer.clear();
		}

		this.recent.clear();
	}

	@Override
	public NamespacedKey getKey() {
		return key;
	}

	/**
	 * Get a singleton instance of the expansive pattern.
	 *
	 * @return the expansive pattern
	 */
	public static PatternExpansive get() {
		return (instance == null) ? instance = new PatternExpansive() : instance;
	}

}