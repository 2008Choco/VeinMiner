package wtf.choco.veinminer.platform;

import java.io.File;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.config.VeinMinerConfiguration;
import wtf.choco.veinminer.platform.world.BlockState;
import wtf.choco.veinminer.platform.world.BlockType;
import wtf.choco.veinminer.platform.world.ItemType;

/**
 * A bridge interface for various implementing platforms.
 */
public interface ServerPlatform {

    /**
     * Get VeinMiner's version.
     *
     * @return the vein miner version
     */
    @NotNull
    public String getVeinMinerVersion();

    /**
     * Get the {@link File} directory where VeinMiner's files are located.
     *
     * @return VeinMiner's plugin directory
     */
    @NotNull
    public File getVeinMinerPluginDirectory();

    /**
     * Get VeinMiner's {@link Logger}.
     *
     * @return the logger
     */
    @NotNull
    public Logger getLogger();

    /**
     * Get the {@link VeinMinerConfiguration} instance for this server platform.
     *
     * @return the config
     */
    @NotNull
    public VeinMinerConfiguration getConfig();

    /**
     * Construct a new {@link BlockState} from a string.
     * <p>
     * The string passed to this method must be a fully-qualified state understood by
     * Minecraft. For instance, {@code "minecraft:chest[waterlogged=true,facing=south]"}
     * (excluding the quotation marks). States are entirely optional and a simple block
     * type may be passed as well, in which case the default BlockState will be returned.
     * <pre>
     * getState("minecraft:chest[waterlogged=true,facing=south]");
     * getState("button[powered=false]");
     * getState("minecraft:air");
     * getState("torch");
     * </pre>
     *
     * @param state the state string from which to create a BlockState
     *
     * @return the created BlockState, or null if an invalid string
     */
    @Nullable
    public BlockState getState(@NotNull String state);

    /**
     * Construct a new {@link BlockType} from a string.
     * <p>
     * The string passed to this method must be a type of block registered to Minecraft.
     * <pre>
     * getBlockType("minecraft:stone");
     * getBlockType("diamond_ore");
     * </pre>
     *
     * @param type the type string from which to create a BlockType
     *
     * @return the created BlockType, or null if an unknown type
     */
    @Nullable
    public BlockType getBlockType(@NotNull String type);

    /**
     * Construct a new {@link ItemType} from a string.
     * <p>
     * The string passed to this method must be a type of item registered to Minecraft.
     * <pre>
     * getBlockType("minecraft:apple");
     * getBlockType("diamond_ore"); // While it is a block, it also has an item
     * </pre>
     *
     * @param type the type string from which to create an ItemType
     *
     * @return the created ItemType, or null if an unknown type
     */
    @Nullable
    public ItemType getItemType(@NotNull String type);

    /**
     * Create a {@link PlatformPlayer} instance for the player with the given UUID.
     *
     * @param playerUUID the player UUID
     *
     * @return the platform player
     */
    @NotNull
    public PlatformPlayer getPlatformPlayer(@NotNull UUID playerUUID);

    /**
     * Get all online players.
     *
     * @return all online players
     */
    @NotNull
    public Collection<? extends PlatformPlayer> getOnlinePlayers();

    /**
     * Get the {@link PlatformPermission} with the given name, or register it with the given description
     * and default value if it does not exist.
     *
     * @param permission the permission name to get (or register)
     * @param description a description provider if the permission does not exist and needs to be registered
     * @param permissionDefault the default permission value
     *
     * @return the permission
     */
    @NotNull
    public PlatformPermission getOrRegisterPermission(String permission, Supplier<String> description, PlatformPermission.Default permissionDefault);

    /**
     * Get an instance of the {@link ServerEventDispatcher}.
     *
     * @return the event dispatcher
     */
    @NotNull
    public ServerEventDispatcher getEventDispatcher();

    /**
     * Run the given {@link Runnable} task the given amount of ticks later.
     *
     * @param runnable the task to run
     * @param ticks the amount of time (in ticks) after which to run the task
     */
    public void runTaskLater(@NotNull Runnable runnable, int ticks);

}
