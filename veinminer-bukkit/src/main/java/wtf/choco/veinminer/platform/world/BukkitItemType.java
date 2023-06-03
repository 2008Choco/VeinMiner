package wtf.choco.veinminer.platform.world;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.util.NamespacedKey;

/**
 * A Bukkit implementation of {@link ItemType}.
 */
public final class BukkitItemType implements ItemType {

    private static final Map<org.bukkit.inventory.ItemType, ItemType> CACHE = new HashMap<>();

    private final org.bukkit.inventory.ItemType bukkit;
    private final NamespacedKey key;

    private BukkitItemType(@NotNull org.bukkit.inventory.ItemType material) {
        this.bukkit = material;

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
        return bukkit == org.bukkit.inventory.ItemType.AIR;
    }

    /**
     * Get the Bukkit {@link org.bukkit.inventory.ItemType ItemType} represented by this {@link BukkitItemType}.
     *
     * @return the material
     */
    @NotNull
    public org.bukkit.inventory.ItemType getBukkit() {
        return bukkit;
    }

    /**
     * Get a VeinMiner {@link ItemType} for the given Bukkit {@link org.bukkit.inventory.ItemType ItemType}.
     *
     * @param bukkitItemType the item type
     *
     * @return the item type
     */
    @NotNull
    public static ItemType of(@NotNull org.bukkit.inventory.ItemType bukkitItemType) {
        return CACHE.computeIfAbsent(bukkitItemType, BukkitItemType::new);
    }

    @Override
    public int hashCode() {
        return bukkit.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof BukkitItemType other && bukkit == other.bukkit);
    }

    @Override
    public String toString() {
        return String.format("BukkitItemType[key=%s]", bukkit.getKey());
    }

}
