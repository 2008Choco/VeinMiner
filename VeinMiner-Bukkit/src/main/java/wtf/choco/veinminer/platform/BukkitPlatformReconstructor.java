package wtf.choco.veinminer.platform;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BukkitPlatformReconstructor implements PlatformReconstructor {

    public static final PlatformReconstructor INSTANCE = new BukkitPlatformReconstructor();

    private BukkitPlatformReconstructor() { }

    @Nullable
    @Override
    public BlockState getState(@NotNull String state) {
        try {
            return BukkitBlockState.of(Bukkit.createBlockData(state));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Nullable
    @Override
    public BlockType getBlockType(@NotNull String type) {
        Material material = Material.matchMaterial(type);
        return (material != null) ? BukkitBlockType.of(material) : null;
    }

    @Nullable
    @Override
    public ItemType getItemType(@NotNull String type) {
        Material material = Material.matchMaterial(type);
        return (material != null) ? BukkitItemType.of(material) : null;
    }

}
