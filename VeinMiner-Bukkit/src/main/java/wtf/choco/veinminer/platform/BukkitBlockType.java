package wtf.choco.veinminer.platform;

import java.util.EnumMap;
import java.util.Map;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.util.NamespacedKey;

public final class BukkitBlockType implements BlockType {

    private static final Map<Material, BlockType> CACHE = new EnumMap<>(Material.class);

    private final Material material;
    private final NamespacedKey key;

    private BukkitBlockType(@NotNull Material material) {
        this.material = material;

        org.bukkit.NamespacedKey key = material.getKey();
        this.key = new NamespacedKey(key.getNamespace(), key.getKey());
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return key;
    }

    @NotNull
    @Override
    public BlockState createBlockState(@NotNull String states) {
        return BukkitBlockState.of(material.createBlockData(states));
    }

    @NotNull
    public Material getMaterial() {
        return material;
    }

    @NotNull
    public static BlockType of(@NotNull Material material) {
        return CACHE.computeIfAbsent(material, BukkitBlockType::new);
    }

    @Override
    public int hashCode() {
        return material.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof BukkitBlockType other && material == other.material);
    }

    @Override
    public String toString() {
        return material.toString();
    }

}
