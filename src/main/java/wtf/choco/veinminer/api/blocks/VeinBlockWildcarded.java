package wtf.choco.veinminer.api.blocks;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import wtf.choco.veinminer.api.VeinTool;

public class VeinBlockWildcarded extends VeinBlock {
	
	public VeinBlockWildcarded(Material type, VeinTool... tools) {
		super(type);
		
		for (VeinTool tool : tools) {
			this.setVeinmineableBy(tool, true);
		}
	}
	
	public VeinBlockWildcarded(Material type) {
		super(type);
	}
	
	@Override
	public BlockData getData() {
		return null;
	}
	
	@Override
	public boolean isWildcarded() {
		return true;
	}
	
	@Override
	public boolean isSimilar(Block block) {
		return block != null && block.getType() == type;
	}
	
	@Override
	public boolean isSimilar(BlockData blockData) {
		return blockData != null && blockData.getMaterial() == type;
	}
	
	@Override
	public int hashCode() {
		return 31 * type.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		return (object == this) || (object instanceof VeinBlockWildcarded && ((VeinBlockWildcarded) object).type == type);
	}
	
	@Override
	public String toString() {
		return type.getKey().toString();
	}
	
}