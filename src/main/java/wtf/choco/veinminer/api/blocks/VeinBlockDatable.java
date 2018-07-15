package wtf.choco.veinminer.api.blocks;

import java.util.Objects;

import com.google.common.base.Preconditions;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import wtf.choco.veinminer.api.VeinTool;

/**
 * Represents block mineable by VeinMiner. This VeinBlock implementation requires specific data
 * to be declared and it must be identical to those passed to {@link #isSimilar(Block)} (and
 * its overloaded methods)
 */
public class VeinBlockDatable extends VeinBlock {
	
	private final BlockData data;
	
	public VeinBlockDatable(BlockData data, VeinTool... tools) {
		this(data.getMaterial(), data);
		
		for (VeinTool tool : tools) {
			this.setVeinmineableBy(tool, true);
		}
	}

	public VeinBlockDatable(Material type, BlockData data) {
		super(type);
		Preconditions.checkNotNull(data, "data");
		
		this.data = data;
	}
	
	@Override
	public BlockData getData() {
		return data;
	}
	
	@Override
	public boolean isWildcarded() {
		return false;
	}
	
	@Override
	public boolean isSimilar(Block block) {
		return block != null && type == block.getType() && data.equals(block.getBlockData());
	}
	
	@Override
	public boolean isSimilar(BlockData blockData) {
		return blockData != null && type == blockData.getMaterial() && data.equals(blockData);
	}
	
	@Override
	public int hashCode() {
		int result = 31 * type.hashCode();
		result += 31 * result + data.hashCode();
		return result;
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == this) return true;
		if (!(object instanceof VeinBlockDatable)) return false;
		
		VeinBlockDatable other = (VeinBlockDatable) object;
		return type == other.type && Objects.equals(other.data, data);
	}
	
	@Override
	public String toString() {
		return data.getAsString();
	}
	
}