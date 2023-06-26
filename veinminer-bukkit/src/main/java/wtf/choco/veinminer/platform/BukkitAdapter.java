package wtf.choco.veinminer.platform;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.platform.world.BlockAccessor;
import wtf.choco.veinminer.platform.world.BlockState;
import wtf.choco.veinminer.platform.world.BlockType;
import wtf.choco.veinminer.platform.world.BukkitBlockAccessor;
import wtf.choco.veinminer.platform.world.BukkitBlockState;
import wtf.choco.veinminer.platform.world.BukkitBlockType;
import wtf.choco.veinminer.platform.world.BukkitItemStack;
import wtf.choco.veinminer.platform.world.BukkitItemType;
import wtf.choco.veinminer.platform.world.ItemStack;
import wtf.choco.veinminer.platform.world.ItemType;
import wtf.choco.veinminer.util.BlockFace;

/**
 * A utility class to convert between VeinMiner platform instances and the Bukkit
 * types that they wrap.
 */
public final class BukkitAdapter {

    private static final BiMap<org.bukkit.GameMode, GameMode> GAME_MODE_MAP = ImmutableBiMap.<org.bukkit.GameMode, GameMode>builder()
            .put(org.bukkit.GameMode.SURVIVAL, GameMode.SURVIVAL)
            .put(org.bukkit.GameMode.CREATIVE, GameMode.CREATIVE)
            .put(org.bukkit.GameMode.SPECTATOR, GameMode.SPECTATOR)
            .put(org.bukkit.GameMode.SPECTATOR, GameMode.SPECTATOR)
            .build();

    private BukkitAdapter() { }

    /**
     * Convert a Bukkit {@link Player} to a {@link BukkitPlatformPlayer}.
     *
     * @param player the bukkit player
     *
     * @return the platform player
     */
    @NotNull
    public static BukkitPlatformPlayer adapt(@NotNull Player player) {
        return (BukkitPlatformPlayer) BukkitServerPlatform.getInstance().getPlatformPlayer(player.getUniqueId());
    }

    /**
     * Convert a {@link PlatformPlayer} to a Bukkit {@link Player}.
     *
     * @param player the platform player
     *
     * @return the bukkit player
     */
    @NotNull
    public static Player adapt(@NotNull PlatformPlayer player) {
        return ((BukkitPlatformPlayer) player).getPlayerOrThrow();
    }

    /**
     * Convert a Bukkit {@link World} to a {@link BukkitBlockAccessor}.
     *
     * @param world the bukkit world
     *
     * @return the platform block accessor
     */
    @NotNull
    public static BukkitBlockAccessor adapt(@NotNull World world) {
        return BukkitBlockAccessor.forWorld(world);
    }

    /**
     * Convert a {@link BlockAccessor} to a Bukkit {@link World}.
     *
     * @param accessor the platform block accessor
     *
     * @return the bukkit world
     */
    @NotNull
    public static World adapt(@NotNull BlockAccessor accessor) {
        return ((BukkitBlockAccessor) accessor).getWorldOrThrow();
    }

    /**
     * Convert a Bukkit {@link BlockData} to a {@link BukkitBlockState}.
     *
     * @param blockData the bukkit block data
     *
     * @return the platform block state
     */
    @NotNull
    public static BukkitBlockState adapt(@NotNull BlockData blockData) {
        return BukkitBlockState.of(blockData);
    }

    /**
     * Convert a {@link BlockState} to a Bukkit {@link BlockData}.
     *
     * @param state the platform block state
     *
     * @return the bukkit block state
     */
    @NotNull
    public static BlockData adapt(@NotNull BlockState state) {
        return ((BukkitBlockState) state).getBukkitBlockData();
    }

    /**
     * Convert a Bukkit {@link Material} to a {@link BukkitBlockType}.
     *
     * @param material the bukkit material
     *
     * @return the platform block type
     */
    @NotNull
    public static BukkitBlockType adaptBlock(@NotNull Material material) {
        return BukkitBlockType.of(material);
    }

    /**
     * Convert a {@link BlockType} to a Bukkit {@link Material}.
     *
     * @param blockType the platform block type
     *
     * @return the bukkit material
     */
    @NotNull
    public static Material adapt(@NotNull BlockType blockType) {
        return ((BukkitBlockType) blockType).getBukkitMaterial();
    }

    /**
     * Convert a Bukkit {@link org.bukkit.inventory.ItemStack ItemStack} to an {@link BukkitItemStack}.
     *
     * @param itemStack the bukkit item stack
     *
     * @return the platform item stack
     */
    @NotNull
    public static BukkitItemStack adapt(@NotNull org.bukkit.inventory.ItemStack itemStack) {
        return new BukkitItemStack(itemStack);
    }

    /**
     * Convert a {@link ItemStack} to a Bukkit {@link org.bukkit.inventory.ItemStack ItemStack}.
     *
     * @param itemStack the platform item stack
     *
     * @return the bukkit item stack
     */
    @NotNull
    public static org.bukkit.inventory.ItemStack adapt(@NotNull ItemStack itemStack) {
        return ((BukkitItemStack) itemStack).getBukkitItemStack();
    }

    /**
     * Convert a Bukkit {@link Material} to a {@link BukkitItemType}.
     *
     * @param material the bukkit material
     *
     * @return the platform item type
     */
    @NotNull
    public static BukkitItemType adaptItem(@NotNull Material material) {
        return BukkitItemType.of(material);
    }

    /**
     * Convert a {@link ItemType} to a Bukkit {@link Material}.
     *
     * @param itemType the platform item type
     *
     * @return the bukkit material
     */
    @NotNull
    public static Material adapt(@NotNull ItemType itemType) {
        return ((BukkitItemType) itemType).getBukkitMaterial();
    }

    /**
     * Convert a Bukkit {@link org.bukkit.GameMode GameMode} to a {@link GameMode}.
     *
     * @param gameMode the bukkit game mode
     *
     * @return the platform game mode
     */
    @NotNull
    public static GameMode adapt(@NotNull org.bukkit.GameMode gameMode) {
        return GAME_MODE_MAP.get(gameMode);
    }

    /**
     * Convert a {@link GameMode} to a Bukkit {@link org.bukkit.GameMode GameMode}.
     *
     * @param gameMode the platform game mode
     *
     * @return the bukkit game mode
     */
    @NotNull
    public static org.bukkit.GameMode adapt(@NotNull GameMode gameMode) {
        return GAME_MODE_MAP.inverse().get(gameMode);
    }

    /**
     * Convert a Bukkit {@link org.bukkit.block.BlockFace BlockFace} to a {@link BlockFace}.
     *
     * @param blockFace the bukkit block face
     *
     * @return the platform block face
     */
    @NotNull
    public static BlockFace adapt(@NotNull org.bukkit.block.BlockFace blockFace) {
        return BlockFace.valueOf(blockFace.name());
    }

    /**
     * Convert a {@link BlockFace} to a Bukkit {@link org.bukkit.block.BlockFace BlockFace}.
     *
     * @param blockFace the platform block face
     *
     * @return the bukkit block face
     */
    @NotNull
    public static org.bukkit.block.BlockFace adapt(@NotNull BlockFace blockFace) {
        return org.bukkit.block.BlockFace.valueOf(blockFace.name());
    }

}
