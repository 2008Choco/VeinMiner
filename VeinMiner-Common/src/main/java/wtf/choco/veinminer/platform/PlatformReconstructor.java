package wtf.choco.veinminer.platform;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A reconstructor capable of creating instances of interfaces in this package.
 */
public interface PlatformReconstructor {

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

}
