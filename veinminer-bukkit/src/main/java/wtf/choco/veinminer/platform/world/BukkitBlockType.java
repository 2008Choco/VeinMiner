package wtf.choco.veinminer.platform.world;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.util.NamespacedKey;

/**
 * A Bukkit implementation of {@link BlockType}.
 */
public final class BukkitBlockType implements BlockType {

    private static final Map<org.bukkit.block.BlockType<?>, BlockType> CACHE = new HashMap<>();

    private final org.bukkit.block.BlockType<?> bukkit;
    private final NamespacedKey key;

    private BukkitBlockType(@NotNull org.bukkit.block.BlockType<?> material) {
        this.bukkit = material;

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
        return BukkitBlockState.of(bukkit.createBlockData(states));
    }

    /**
     * Get the Bukkit {@link org.bukkit.block.BlockType BlockType} represented by this {@link BukkitBlockType}.
     *
     * @return the block type
     */
    @NotNull
    public org.bukkit.block.BlockType<?> getBukkit() {
        return bukkit;
    }

    /**
     * Get a VeinMiner {@link BlockType} for the given Bukkit {@link org.bukkit.block.BlockType BlockType}.
     *
     * @param bukkitBlockType the block type
     *
     * @return the block type
     */
    @NotNull
    public static BlockType of(@NotNull org.bukkit.block.BlockType<?> bukkitBlockType) {
        return CACHE.computeIfAbsent(bukkitBlockType, BukkitBlockType::new);
    }

    @Override
    public int hashCode() {
        return bukkit.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof BukkitBlockType other && bukkit == other.bukkit);
    }

    @Override
    public String toString() {
        return String.format("BukkitBlockType[key=%s]", bukkit.getKey());
    }

}
