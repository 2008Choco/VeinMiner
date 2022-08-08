package wtf.choco.veinminer.platform;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.platform.world.BlockAccessor;
import wtf.choco.veinminer.platform.world.ItemStack;
import wtf.choco.veinminer.platform.world.RayTraceResult;
import wtf.choco.veinminer.util.NamespacedKey;

/**
 * Represents a platform-independent player.
 */
public interface PlatformPlayer extends PlatformCommandSender {

    /**
     * Check whether or not this player is online the server.
     *
     * @return true if online, false if offline
     */
    public boolean isOnline();

    /**
     * Get the {@link UUID} of this player.
     *
     * @return the uuid
     */
    @NotNull
    public UUID getUniqueId();

    /**
     * Get the {@link BlockAccessor world} in which this player currently resides.
     *
     * @return the world
     */
    @NotNull
    public BlockAccessor getWorld();

    /**
     * Get the {@link ItemStack} in the player's main hand.
     *
     * @return the item stack
     */
    @NotNull
    public ItemStack getItemInMainHand();

    /**
     * Get the block the player is looking at within the given distance. If the player is
     * looking at a block that exceeds the given distance, the result will be empty.
     *
     * @param distance the maximum block distance
     *
     * @return the result of the ray trace
     */
    @NotNull
    public RayTraceResult getTargetBlock(int distance);

    /**
     * Get the {@link GameMode} of this player.
     *
     * @return the game mode
     */
    @NotNull
    public GameMode getGameMode();

    /**
     * Check whether or not this player is sneaking.
     *
     * @return true if sneaking, false otherwise
     */
    public boolean isSneaking();

    /**
     * Send a plugin message to this player over the given channel.
     *
     * @param channel the channel on which to send the message
     * @param message the message contents
     */
    public void sendPluginMessage(@NotNull NamespacedKey channel, byte[] message);

    /**
     * Kick this player from the server with the given disconnect message.
     *
     * @param message the disconnect message
     */
    public void kick(@NotNull String message);

}
