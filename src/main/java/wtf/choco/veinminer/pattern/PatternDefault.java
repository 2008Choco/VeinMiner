package wtf.choco.veinminer.pattern;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;

import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.api.veinutils.MaterialAlias;
import wtf.choco.veinminer.api.veinutils.VeinBlock;
import wtf.choco.veinminer.api.veinutils.VeinTool;
import wtf.choco.veinminer.utils.VBlockFace;

/**
 * The default {@link VeinMiningPattern} implementation used by all players unless explicitly set.
 * 
 * @author Parker Hawke - 2008Choco
 */
public final class PatternDefault implements VeinMiningPattern {
	
	private static final VBlockFace[] LIMITED_FACES = {
			VBlockFace.UP, VBlockFace.DOWN, VBlockFace.NORTH, VBlockFace.SOUTH, VBlockFace.EAST,
			VBlockFace.WEST, VBlockFace.NORTH_EAST, VBlockFace.NORTH_WEST, VBlockFace.SOUTH_EAST,
			VBlockFace.SOUTH_WEST
	};
	
	private static PatternDefault instance;
	
	private final VeinMiner plugin;
	private final NamespacedKey key;
	private final List<Block> blockBuffer = new ArrayList<>();
	
	private PatternDefault() {
		this.plugin = VeinMiner.getPlugin();
		this.key = new NamespacedKey(plugin, "default");
	}
	
	@Override
	public void allocateBlocks(Set<Block> blocks, VeinBlock type, Block origin, VeinTool tool, MaterialAlias alias) {
		int maxVeinSize = tool.getMaxVeinSize();
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
		return type.isSimilar(block) || (alias != null && alias.isAliased(block.getType(), block.getBlockData()));
	}
	
	private VBlockFace[] getFacesToMine() {
		return plugin.getConfig().getBoolean("IncludeEdges") ? VBlockFace.values() : LIMITED_FACES;
	}
	
	/**
	 * Get a singleton instance of the default pattern.
	 * 
	 * @return the default pattern
	 */
	public static PatternDefault get() {
		return (instance == null) ? instance = new PatternDefault() : instance;
	}
	
}