package wtf.choco.veinminer.data.block;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import org.jetbrains.annotations.NotNull;

final class BlockCache<T> {

    static final BlockCache<Material> MATERIAL = new BlockCache<>(new EnumMap<>(Material.class));
    static final BlockCache<BlockData> BLOCK_DATA = new BlockCache<>(new HashMap<>());

    private final Map<T, VeinBlock> cached;

    private BlockCache(@NotNull Map<T, VeinBlock> backingMap) {
        this.cached = backingMap;
    }

    VeinBlock getOrCache(@NotNull T type, @NotNull Function<T, VeinBlock> defaultSupplier) {
        return cached.computeIfAbsent(type, defaultSupplier);
    }

    static void clear() {
        MATERIAL.cached.clear();
        BLOCK_DATA.cached.clear();
    }

}
