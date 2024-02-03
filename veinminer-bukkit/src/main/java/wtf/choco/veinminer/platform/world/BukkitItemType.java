package wtf.choco.veinminer.platform.world;

import com.google.common.base.Preconditions;

import java.util.EnumMap;
import java.util.Map;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import wtf.choco.network.data.NamespacedKey;

/**
 * A Bukkit implementation of {@link ItemType}.
 */
public final class BukkitItemType implements ItemType {

    private static final Map<Material, BukkitItemType> CACHE = new EnumMap<>(Material.class);

    private final Material material;
    private final NamespacedKey key;

    private BukkitItemType(@NotNull Material material) {
        this.material = material;

        org.bukkit.NamespacedKey key = material.getKey();
        this.key = new NamespacedKey(key.getNamespace(), key.getKey());
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return key;
    }

    @Override
    public boolean isAir() {
        return material.isAir();
    }

    /**
     * Get the Bukkit {@link Material} represented by this {@link BukkitItemType}.
     *
     * @return the material
     */
    @NotNull
    public Material getBukkitMaterial() {
        return material;
    }

    /**
     * Get an {@link BukkitItemType} for the given {@link Material}.
     *
     * @param material the material
     *
     * @return the item type
     */
    @NotNull
    public static BukkitItemType of(@NotNull Material material) {
        Preconditions.checkArgument(material.isItem(), "material is not an item");
        return CACHE.computeIfAbsent(material, BukkitItemType::new);
    }

    @Override
    public int hashCode() {
        return material.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof BukkitItemType other && material == other.material);
    }

    @Override
    public String toString() {
        return material.toString();
    }

}
