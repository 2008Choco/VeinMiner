package wtf.choco.veinminer.platform.world;

import com.google.common.base.Preconditions;

import java.util.EnumMap;
import java.util.Map;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.util.NamespacedKey;

/**
 * A Bukkit implementation of {@link BlockType}.
 */
public final class BukkitBlockType implements BlockType {

    private static final Map<Material, BukkitBlockType> CACHE = new EnumMap<>(Material.class);

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

    /**
     * Get the Bukkit {@link Material} represented by this {@link BukkitBlockType}.
     *
     * @return the material
     */
    @NotNull
    public Material getBukkitMaterial() {
        return material;
    }

    /**
     * Get a {@link BukkitBlockType} for the given {@link Material}.
     *
     * @param material the material
     *
     * @return the block type
     */
    @NotNull
    public static BukkitBlockType of(@NotNull Material material) {
        Preconditions.checkArgument(material.isBlock(), "material is not a block");
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
