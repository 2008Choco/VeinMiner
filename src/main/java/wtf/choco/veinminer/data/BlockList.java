package wtf.choco.veinminer.data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import wtf.choco.veinminer.data.block.VeinBlock;

public class BlockList implements Iterable<VeinBlock>, Serializable {

	private static final long serialVersionUID = -2274615459168041997L;

	private final Set<VeinBlock> blocks;

	public BlockList(BlockList... lists) {
		int expectedSize = 0; // Obviously doesn't take into consideration duplicates
		for (BlockList list : lists) {
			expectedSize += list.size();
		}

		this.blocks = new HashSet<>(expectedSize);
		for (BlockList list : lists) {
			this.addAll(list);
		}
	}

	public BlockList(int initialSize) {
		this.blocks = new HashSet<>(initialSize);
	}

	public BlockList() {
		this.blocks = new HashSet<>();
	}

	public VeinBlock add(BlockData data, String rawData) {
		VeinBlock block = VeinBlock.get(data, rawData);
		this.add(block);
		return block;
	}

	public VeinBlock add(Material material) {
		// Remove any specific data with this type before adding a wildcard
		Iterator<VeinBlock> iterator = iterator();
		while (iterator.hasNext()) {
			if (iterator.next().getType() == material) {
				iterator.remove();
			}
		}

		VeinBlock block = VeinBlock.get(material);
		this.add(block);
		return block;
	}

	public boolean add(VeinBlock block) {
		return blocks.add(block);
	}

	public boolean addAll(Iterable<? extends VeinBlock> values) {
		boolean changed = false;

		for (VeinBlock block : values) {
			changed |= blocks.add(block);
		}

		return changed;
	}

	public void remove(VeinBlock block) {
		this.blocks.remove(block);
	}

	public void remove(BlockData data) {
		this.blocks.removeIf(block -> block.getBlockData().equals(data));
	}

	public void removeAll(Material material) {
		this.blocks.removeIf(block -> block.getType() == material);
	}

	public boolean contains(VeinBlock block) {
		return blocks.contains(block);
	}

	public boolean contains(BlockData data) {
		return containsOnPredicate(block -> block.encapsulates(data));
	}

	public boolean contains(Material material) {
		return containsOnPredicate(block -> block.getType() == material);
	}

	public boolean containsExact(BlockData data) {
		return containsOnPredicate(block -> block.getBlockData().equals(data));
	}

	private boolean containsOnPredicate(Predicate<VeinBlock> predicate) {
		for (VeinBlock block : blocks) {
			if (predicate.test(block)) {
				return true;
			}
		}

		return false;
	}

	public int size() {
		return blocks.size();
	}

	public void clear() {
		this.blocks.clear();
	}

	@Override
	public Iterator<VeinBlock> iterator() {
		return blocks.iterator();
	}

	@Override
	public int hashCode() {
		return blocks.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return blocks.equals(obj);
	}

}