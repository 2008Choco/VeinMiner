package wtf.choco.veinminer.data.block;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

class VeinBlockMaterial implements VeinBlock {

	private final Material type;
	private final BlockData data;

	protected VeinBlockMaterial(Material type) {
		this.type = type;
		this.data = type.createBlockData("[]");
	}

	@Override
	public Material getType() {
		return type;
	}

	@Override
	public boolean hasSpecificData() {
		return false;
	}

	@Override
	public BlockData getBlockData() {
		return data.clone();
	}

	@Override
	public boolean encapsulates(Block block) {
		return block != null && block.getType() == type;
	}

	@Override
	public boolean encapsulates(BlockData data) {
		return data != null && data.getMaterial() == type;
	}

	@Override
	public boolean encapsulates(Material material) {
		return material == type;
	}

	@Override
	public String asDataString() {
		return type.getKey().toString();
	}

	@Override
	public int hashCode() {
		return type.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj == type;
	}

	@Override
	public VeinBlock clone() {
		return new VeinBlockMaterial(type);
	}

	@Override
	public String toString() {
		return "{VeinBlockMaterial:{\"Type\":" + asDataString() + "\"}}";
	}

}