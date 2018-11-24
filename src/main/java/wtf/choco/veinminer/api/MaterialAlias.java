package wtf.choco.veinminer.api;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.common.base.Preconditions;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import wtf.choco.veinminer.api.blocks.VeinBlock;

/**
 * Represents an aliasing between multiple {@link VeinBlock}s which VeinMiner can recognise as a
 * single material value when being vein mined.
 */
public class MaterialAlias implements Iterable<VeinBlock>, Cloneable {

	private final Set<VeinBlock> blocks = new HashSet<>();

	/**
	 * Construct a new alias between varying vein blocks.
	 *
	 * @param blocks the blocks to alias
	 */
	public MaterialAlias(VeinBlock... blocks) {
		for (VeinBlock block : blocks) {
			this.blocks.add(block);
		}
	}

	private MaterialAlias(Set<VeinBlock> blocks) {
		this.blocks.addAll(blocks);
	}

	/**
	 * Add a block to this alias.
	 *
	 * @param block the block to add
	 */
	public void addAlias(VeinBlock block) {
		Preconditions.checkNotNull(block, "Cannot add a null alias");
		this.blocks.add(block);
	}

	/**
	 * Remove a NVeinBlock from this alias.
	 *
	 * @param block the block to remove
	 */
	public void removeAlias(VeinBlock block) {
		this.blocks.remove(block);
	}

	/**
	 * Remove a material with specific block data from this alias.
	 *
	 * @param data the data to remove
	 */
	public void removeAlias(BlockData data) {
		Iterator<VeinBlock> blockIterator = this.iterator();

		while (blockIterator.hasNext()) {
			if (blockIterator.next().isSimilar(data)) {
				blockIterator.remove();
			}
		}
	}

	/**
	 * Remove a material without any specific block data from this alias.
	 *
	 * @param material the material to remove
	 */
	public void removeAlias(Material material) {
		Iterator<VeinBlock> blockIterator = this.iterator();

		while (blockIterator.hasNext()) {
			if (blockIterator.next().getType() == material) {
				blockIterator.remove();
			}
		}
	}

	/**
	 * Check whether a block is aliased under this material alias.
	 *
	 * @param block the block to check
	 *
	 * @return true if aliased, false otherwise
	 */
	public boolean isAliased(VeinBlock block) {
		return blocks.contains(block);
	}

	/**
	 * Check whether a material with specific block data is aliased under this material alias.
	 *
	 * @param data the data to check
	 *
	 * @return true if aliased, false otherwise
	 */
	public boolean isAliased(BlockData data) {
		return blocks.stream().anyMatch(b -> b.isSimilar(data));
	}

	/**
	 * Check whether a block is aliased under this material alias.
	 *
	 * @param block the block to check
	 *
	 * @return true if aliased, false otherwise
	 */
	public boolean isAliased(Block block) {
		return block != null && isAliased(block.getBlockData());
	}

	/**
	 * Check whether a material with no specific block data is aliased under this material alias.
	 *
	 * @param material the material to check
	 *
	 * @return true if aliased, false otherwise
	 */
	public boolean isAliased(Material material) {
		return blocks.stream().anyMatch(b -> b.getType() == material);
	}

	/**
	 * Get all blocks that are considered under this alias. A copy of the set is returned,
	 * therefore any changes made to the returned set will not affect this MaterialAlias.
	 *
	 * @return all aliased blocks
	 */
	public Set<VeinBlock> getAliasedBlocks() {
		return new HashSet<>(blocks);
	}

	@Override
	public Iterator<VeinBlock> iterator() {
		return blocks.iterator();
	}

	@Override
	public MaterialAlias clone() {
		return new MaterialAlias(blocks);
	}

	@Override
	public int hashCode() {
		return 31 * blocks.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (object == this) return true;
		if (!(object instanceof MaterialAlias)) return false;

		MaterialAlias alias = (MaterialAlias) object;
		return blocks.equals(alias.blocks);
	}

}