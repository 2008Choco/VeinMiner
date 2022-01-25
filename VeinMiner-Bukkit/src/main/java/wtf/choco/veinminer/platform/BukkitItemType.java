package wtf.choco.veinminer.platform;

import com.google.common.base.Preconditions;

import java.util.EnumMap;
import java.util.Map;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.util.NamespacedKey;

/**
 * A Bukkit implementation of {@link ItemType}.
 */
public final class BukkitItemType implements ItemType {

    private static final Map<Material, ItemType> CACHE = new EnumMap<>(Material.class);

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

    @NotNull
    public Material getMaterial() {
        return material;
    }

    /**
     * Get an {@link ItemType} for the given {@link Material}.
     *
     * @param material the material
     *
     * @return the item type
     */
    @NotNull
    public static ItemType of(@NotNull Material material) {
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
