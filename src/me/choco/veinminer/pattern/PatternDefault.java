package me.choco.veinminer.pattern;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import me.choco.veinminer.VeinMiner;
import me.choco.veinminer.api.veinutils.MaterialAlias;
import me.choco.veinminer.api.veinutils.VeinTool;
import me.choco.veinminer.utils.VBlockFace;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;

public class PatternDefault implements VeinMiningPattern {
	
	private static final VBlockFace[] LIMITED_FACES = {
			VBlockFace.UP, VBlockFace.DOWN, VBlockFace.NORTH, VBlockFace.SOUTH, VBlockFace.EAST,
			VBlockFace.WEST, VBlockFace.NORTH_EAST, VBlockFace.NORTH_WEST, VBlockFace.SOUTH_EAST,
			VBlockFace.SOUTH_WEST
	};
	
	private final VeinMiner plugin;
	private final NamespacedKey key;
	private final List<Block> blockBuffer = new ArrayList<>();
	
	public PatternDefault() {
		this.plugin = VeinMiner.getPlugin();
		this.key = new NamespacedKey(plugin, "default");
	}
	
	@Override
	public void allocateBlocks(Set<Block> blocks, Block origin, VeinTool tool, MaterialAlias alias) {
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
					if (blocks.contains(nextBlock) || !blockIsSameMaterial(origin, nextBlock, alias)) {
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
	
	private boolean blockIsSameMaterial(Block original, Block block, MaterialAlias alias) {
		if (original.getBlockData().equals(block.getBlockData())) return true;
		return alias != null && alias.isAliased(block.getType(), block.getBlockData());
	}
	
	private VBlockFace[] getFacesToMine() {
		return plugin.getConfig().getBoolean("IncludeEdges") ? VBlockFace.values() : LIMITED_FACES;
	}
	
}