package wtf.choco.veinminer.platform;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.platform.world.BlockAccessor;
import wtf.choco.veinminer.platform.world.BukkitBlockAccessor;
import wtf.choco.veinminer.platform.world.BukkitItemStack;
import wtf.choco.veinminer.platform.world.ItemStack;
import wtf.choco.veinminer.platform.world.RayTraceResult;
import wtf.choco.veinminer.util.BlockFace;
import wtf.choco.veinminer.util.BlockPosition;
import wtf.choco.veinminer.util.NamespacedKey;

/**
 * Bukkit implementation of {@link PlatformPlayer}.
 */
public final class BukkitPlatformPlayer implements PlatformPlayer {

    private Reference<Player> player;

    private String name;
    private GameMode lastKnownGameMode;

    private final UUID playerUUID;

    BukkitPlatformPlayer(@NotNull UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.player = new WeakReference<>(Bukkit.getPlayer(playerUUID));
    }

    @Override
    public boolean isOnline() {
        Player player = getPlayer();
        return player != null && player.isOnline();
    }

    @NotNull
    @Override
    public String getName() {
        if (name == null) {
            Player player = getPlayer();
            if (player != null) {
                this.name = player.getName();
            }
        }

        return name;
    }

    @NotNull
    @Override
    public BlockAccessor getWorld() {
        Player player = getPlayerOrThrow();
        return BukkitBlockAccessor.forWorld(player.getWorld());
    }

    @NotNull
    @Override
    public ItemStack getItemInMainHand() {
        Player player = getPlayerOrThrow();
        return new BukkitItemStack(player.getInventory().getItemInMainHand());
    }

    @NotNull
    @Override
    public RayTraceResult getTargetBlock(int distance) {
        Player player = getPlayerOrThrow();

        org.bukkit.util.RayTraceResult rayTraceResult = player.rayTraceBlocks(distance, FluidCollisionMode.NEVER);
        Block targetBlock;
        org.bukkit.block.BlockFace targetBlockFace;

        if (rayTraceResult == null || (targetBlock = rayTraceResult.getHitBlock()) == null || (targetBlockFace = rayTraceResult.getHitBlockFace()) == null) {
            return new RayTraceResult();
        }

        return new RayTraceResult(new BlockPosition(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ()), BlockFace.valueOf(targetBlockFace.name()));
    }

    @NotNull
    @Override
    public UUID getUniqueId() {
        return playerUUID;
    }

    @Override
    public void sendMessage(@NotNull String message) {
        Player player = getPlayer();
        if (player == null) {
            return;
        }

        player.sendMessage(message);
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        Player player = getPlayer();
        return player != null && player.hasPermission(permission);
    }

    @NotNull
    @Override
    public GameMode getGameMode() {
        Player player = getPlayer();
        if (player == null) {
            return lastKnownGameMode;
        }

        GameMode gameMode = GameMode.getById(player.getGameMode().name());
        if (gameMode == null) {
            throw new IllegalStateException("Unknown game mode \"" + player.getGameMode() + "\". This is a bug.");
        }

        return (lastKnownGameMode = gameMode);
    }

    @Override
    public boolean isSneaking() {
        Player player = getPlayer();
        return player != null && player.isSneaking();
    }

    @Override
    public void sendPluginMessage(@NotNull NamespacedKey channel, byte[] message) {
        Player player = getPlayer();
        if (player == null) {
            return;
        }

        player.sendPluginMessage(VeinMinerPlugin.getInstance(), channel.toString(), message);
    }

    @Override
    public void kick(@NotNull String message) {
        Player player = getPlayer();
        if (player == null) {
            return;
        }

        player.kickPlayer(message);
    }

    /**
     * Get the Bukkit {@link Player} represented by this {@link BukkitPlatformPlayer}, or null
     * if the player is not online.
     *
     * @return the player, or null
     */
    @Nullable
    public Player getPlayer() {
        Player player = this.player.get();
        if (player == null || !player.isValid()) {
            this.player = new WeakReference<>(Bukkit.getPlayer(playerUUID));
        }

        return this.player.get();
    }

    /**
     * Get the Bukkit {@link Player} represented by this {@link BukkitPlatformPlayer}, or throw
     * an {@link IllegalStateException} if the player is not online.
     *
     * @return the player
     */
    @NotNull
    public Player getPlayerOrThrow() {
        Player player = getPlayer();
        if (player == null) {
            throw new IllegalStateException("player is not online");
        }

        return player;
    }

    @Override
    public int hashCode() {
        return playerUUID.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof BukkitPlatformPlayer other && playerUUID.equals(other.playerUUID));
    }

}
