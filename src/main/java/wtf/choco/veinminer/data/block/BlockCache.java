package wtf.choco.veinminer.data.block;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

final class BlockCache<T> {

	protected static final BlockCache<Material> MATERIAL = new BlockCache<>(new EnumMap<>(Material.class));
	protected static final BlockCache<BlockData> BLOCK_DATA = new BlockCache<>(new HashMap<>());

	private final Map<T, VeinBlock> cached;

	private BlockCache(Map<T, VeinBlock> backingMap) {
		this.cached = backingMap;
	}

	protected VeinBlock getOrCache(T type, Function<T, VeinBlock> defaultSupplier) {
		return cached.computeIfAbsent(type, defaultSupplier);
	}

	protected static void clear() {
		MATERIAL.cached.clear();
		BLOCK_DATA.cached.clear();
	}

}