package wtf.choco.veinminer.platform.world;

import org.jetbrains.annotations.NotNull;

import wtf.choco.network.data.NamespacedKey;

/**
 * Represents a type of block.
 */
public interface BlockType {

    /**
     * Get the {@link NamespacedKey} of this {@link BlockType}.
     *
     * @return the key
     */
    @NotNull
    public NamespacedKey getKey();

    /**
     * Create a {@link BlockState} of this {@link BlockType} with the given state string.
     * <p>
     * The {@code states} string should contain only the comma-delimited states of a block,
     * excluding its type and the square brackets that surround the states in typical Minecraft
     * command inputs. For instance, to create a blockstate with the states
     * {@code waterlogged=false} and {@code facing=north}, the {@code states} string should be
     * {@code "waterlogged=false,facing=north"} (excluding the quotation marks).
     * <pre>
     * createBlockState("waterlogged=false,facing=north");
     * createBlockState("facing=south");
     * createBlockState("waterlogged=true,type=double");
     * </pre>
     *
     * @param states the states with which to create a BlockState
     *
     * @return the created BlockState
     *
     * @throws IllegalArgumentException if the supplied state string is in an unsupported
     * format and cannot be understood nor parsed correctly by VeinMiner or Minecraft
     */
    @NotNull
    public BlockState createBlockState(@NotNull String states);

}
