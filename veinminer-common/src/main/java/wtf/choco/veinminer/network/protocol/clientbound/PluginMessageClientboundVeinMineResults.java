package wtf.choco.veinminer.network.protocol.clientbound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.documentation.Documentation;
import wtf.choco.veinminer.documentation.MessageField;
import wtf.choco.veinminer.documentation.ProtocolMessageDocumentation;
import wtf.choco.veinminer.network.PluginMessage;
import wtf.choco.veinminer.network.PluginMessageByteBuffer;
import wtf.choco.veinminer.network.protocol.ClientboundPluginMessageListener;
import wtf.choco.veinminer.network.protocol.serverbound.PluginMessageServerboundRequestVeinMine;
import wtf.choco.veinminer.util.BlockPosition;

/**
 * A client bound {@link PluginMessage} including the following data:
 * <ol>
 *   <li><strong>VarInt</strong>: The amount of block keys
 *   <li><strong>Array of BlockPosition</strong>: The block positions that were vein mined
 * </ol>
 * Sent in response to the client sending a {@link PluginMessageServerboundRequestVeinMine}.
 */
public final class PluginMessageClientboundVeinMineResults implements PluginMessage<ClientboundPluginMessageListener> {

    private final List<BlockPosition> blockPositions;

    /**
     * Construct a new {@link PluginMessageClientboundVeinMineResults}.
     *
     * @param blockPositions the calculated {@link BlockPosition BlockPositions}
     */
    public PluginMessageClientboundVeinMineResults(@NotNull Collection<BlockPosition> blockPositions) {
        this.blockPositions = new ArrayList<>(blockPositions);
    }

    /**
     * Construct a new {@link PluginMessageClientboundVeinMineResults} with no positions.
     */
    public PluginMessageClientboundVeinMineResults() {
        this(Collections.emptyList());
    }

    /**
     * Construct a new {@link PluginMessageClientboundVeinMineResults} with input.
     *
     * @param buffer the input buffer
     */
    @Internal
    public PluginMessageClientboundVeinMineResults(@NotNull PluginMessageByteBuffer buffer) {
        BlockPosition[] blockPositions = new BlockPosition[buffer.readVarInt()];

        for (int i = 0; i < blockPositions.length; i++) {
            blockPositions[i] = buffer.readBlockPosition();
        }

        this.blockPositions = Arrays.asList(blockPositions);
    }

    /**
     * Get a {@link List} of all {@link BlockPosition BlockPositions} resulting from
     * the vein mine. May be empty if the vein mine was unsuccessful.
     *
     * @return the block positions
     */
    public List<BlockPosition> getBlockPositions() {
        return blockPositions;
    }

    @Override
    public void write(@NotNull PluginMessageByteBuffer buffer) {
        buffer.writeVarInt(blockPositions.size());
        this.blockPositions.forEach(buffer::writeBlockPosition);
    }

    @Override
    public void handle(@NotNull ClientboundPluginMessageListener listener) {
        listener.handleVeinMineResults(this);
    }

    @Documentation
    private static void document(ProtocolMessageDocumentation.Builder documentation) {
        documentation.name("Vein Mine Results")
            .description("""
                    Sent in response to a client's Request Vein Mine including all block positions as a result of a vein mine at the client's target block and currently active tool category (according to the tool in the player's hand at the time the message was received by the server).
                    """)
            .field(MessageField.TYPE_VARINT, "Size", "The amount of block positions that were included in the resulting vein mine")
            .field(MessageField.TYPE_ARRAY_OF.apply(MessageField.TYPE_BLOCK_POSITION), "Positions", "An array containing all block positions that would be vein mined by the server");
    }

}
