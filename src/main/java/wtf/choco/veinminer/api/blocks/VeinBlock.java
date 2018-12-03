package wtf.choco.veinminer.api.blocks;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import com.google.common.base.Preconditions;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import wtf.choco.veinminer.tool.ToolCategory;

/**
 * Represents block mineable by VeinMiner. A VeinBlock may or may not possess additional data
 * beyond a {@link Material} type.
 */
public class VeinBlock {

	private final Set<ToolCategory> tools = EnumSet.noneOf(ToolCategory.class);

	private final BlockData data;
	private final String rawData;

	public VeinBlock(Material type) {
		this.data = type.createBlockData("[]");
		this.rawData = type.getKey().toString();
	}

	public VeinBlock(BlockData data, String rawData) {
		Preconditions.checkState(Bukkit.createBlockData(rawData) != null, "The block data created must comply with the restrictions imposed by Bukkit#createBlockData(String)");

		this.data = data.clone();
		this.rawData = rawData;
	}

	public VeinBlock(Material type, ToolCategory... tools) {
		this(type);

		for (ToolCategory tool : tools) {
			this.tools.add(tool);
		}
	}

	public VeinBlock(BlockData data, String rawData, ToolCategory... tools) {
		this(data, rawData);

		for (ToolCategory tool : tools) {
			this.tools.add(tool);
		}
	}

	@Deprecated
	public VeinBlock(BlockData data, ToolCategory... tools) {
		this(data);

		for (ToolCategory tool : tools) {
			this.tools.add(tool);
		}
	}

	@Deprecated
	public VeinBlock(BlockData data) {
		this.data = data;
		this.rawData = data.toString();
	}

	/**
	 * Get the type of {@link Material} represented by this VeinBlock instance.
	 *
	 * @return the Bukkit material
	 */
	public Material getType() {
		return data.getMaterial();
	}

	/**
	 * Set whether the specified {@link ToolCategory} is capable of mining this VeinBlock with vein miner
	 * or not.
	 *
	 * @param tool the tool to set
	 * @param mineable true if should be mineable, false otherwise
	 */
	public void setVeinmineableBy(ToolCategory tool, boolean mineable) {
		if (mineable) {
			this.tools.add(tool);
		} else {
			this.tools.remove(tool);
		}
	}

	/**
	 * Check whether the specified {@link ToolCategory} is capable of mining this VeinBlock with vein miner
	 * or not.
	 *
	 * @param tool the tool to check
	 *
	 * @return true if mineable, false otherwise
	 */
	public boolean isVeinmineableBy(ToolCategory tool) {
		return tools.contains(tool);
	}

	/**
	 * Get an immutable set of all tools capable of vein mining this VeinBlock.
	 *
	 * @return all tools
	 */
	public Set<ToolCategory> getVeinmineableBy() {
		return Collections.unmodifiableSet(tools);
	}

	/**
	 * Get the specific data associated with this block, if any. Will never be null. The returned BlockData
	 * instance is immutable and will have no effect on the underlying block data
	 *
	 * @return the associated data
	 */
	public BlockData getData() {
		return data.clone();
	}

	/**
	 * Get the raw data that represents this VeinBlock in a more accurate way. The raw data may or may not
	 * be equal to the result of {@link BlockData#getAsString()}.
	 *
	 * @return the raw data
	 */
	public String getRawData() {
		return rawData;
	}

	/**
	 * Check whether the provided block is encapsulated by this VeinBlock or not.
	 *
	 * @param block the block to check
	 *
	 * @return true if similar, false otherwise
	 */
	public boolean isSimilar(Block block) {
		return block.getBlockData().matches(data);
	}

	/**
	 * Check whether the provided block data is encapsulated by this VeinBlock or not.
	 *
	 * @param blockData the block data to check
	 *
	 * @return true if similar, false otherwise
	 */
	public boolean isSimilar(BlockData blockData) {
		return blockData.matches(data);
	}

	@Override
	public int hashCode() {
		return 31 * data.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		return object == this || (object instanceof VeinBlock && data.equals(((VeinBlock) object).data));
	}

	@Override
	public String toString() {
		return "{VeinBlock:{Type:" + data.getMaterial() + ", Data:\"" + data.getAsString() + "\", RawData:\"" + rawData + "\"}}";
	}

}