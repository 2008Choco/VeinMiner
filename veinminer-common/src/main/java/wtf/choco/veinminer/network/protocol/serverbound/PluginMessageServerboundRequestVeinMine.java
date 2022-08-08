package wtf.choco.veinminer.network.protocol.serverbound;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.documentation.Documentation;
import wtf.choco.veinminer.documentation.MessageField;
import wtf.choco.veinminer.documentation.ProtocolMessageDocumentation;
import wtf.choco.veinminer.network.PluginMessage;
import wtf.choco.veinminer.network.PluginMessageByteBuffer;
import wtf.choco.veinminer.network.protocol.ServerboundPluginMessageListener;
import wtf.choco.veinminer.util.BlockPosition;

/**
 * A server bound {@link PluginMessage} including the following data:
 * <ol>
 *   <li><strong>BlockPosition</strong>: the block position at which to vein mine
 * </ol>
 * Sent by the client to request a vein mine at the player's target block position.
 */
public final class PluginMessageServerboundRequestVeinMine implements PluginMessage<ServerboundPluginMessageListener> {

    private final BlockPosition position;

    /**
     * Construct a new {@link PluginMessageServerboundRequestVeinMine}.
     *
     * @param position the origin
     */
    public PluginMessageServerboundRequestVeinMine(@NotNull BlockPosition position) {
        this.position = position;
    }

    /**
     * Construct a new {@link PluginMessageServerboundRequestVeinMine}.
     *
     * @param x the x coordinate of the origin
     * @param y the y coordinate of the origin
     * @param z the z coordinate of the origin
     */
    public PluginMessageServerboundRequestVeinMine(int x, int y, int z) {
        this(new BlockPosition(x, y, z));
    }

    /**
     * Construct a new {@link PluginMessageServerboundRequestVeinMine} with input.
     *
     * @param buffer the input buffer
     */
    @Internal
    public PluginMessageServerboundRequestVeinMine(@NotNull PluginMessageByteBuffer buffer) {
        this.position = buffer.readBlockPosition();
    }

    /**
     * Get the origin {@link BlockPosition}.
     *
     * @return the origin position
     */
    @NotNull
    public BlockPosition getPosition() {
        return position;
    }

    @Override
    public void write(@NotNull PluginMessageByteBuffer buffer) {
        buffer.writeBlockPosition(position);
    }

    @Override
    public void handle(@NotNull ServerboundPluginMessageListener listener) {
        listener.handleRequestVeinMine(this);
    }

    @Documentation
    private static void document(ProtocolMessageDocumentation.Builder documentation) {
        documentation.name("Request Vein Mine")
            .description("""
                    Sent by the client to request the server to perform a no-op vein mine on the block at which the player is currently looking. The player's active tool category, and all other vein miner required information is calculated on the server, not by the client, exception to the provided origin position.

                    Note that if the player's target block is also calculated on the server but the server will make use of the position sent by the client such that it is within 2 blocks of the server calculated block position. If the position sent to the server exceeds the 2 block distance limit, the server will respond with an empty vein mine result.
                    """)
            .field(MessageField.TYPE_BLOCK_POSITION, "Origin", "The position at which to initiate vein miner");
    }

}
