package wtf.choco.veinminer.network.protocol.clientbound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import wtf.choco.network.Message;
import wtf.choco.network.MessageByteBuffer;
import wtf.choco.veinminer.documentation.Documentation;
import wtf.choco.veinminer.documentation.MessageField;
import wtf.choco.veinminer.documentation.ProtocolMessageDocumentation;
import wtf.choco.veinminer.network.protocol.VeinMinerClientboundMessageListener;
import wtf.choco.veinminer.network.protocol.serverbound.ServerboundRequestVeinMine;
import wtf.choco.veinminer.util.BlockPosition;

/**
 * A client bound {@link Message} including the following data:
 * <ol>
 *   <li><strong>VarInt</strong>: The amount of block keys
 *   <li><strong>Array of BlockPosition</strong>: The block positions that were vein mined
 * </ol>
 * Sent in response to the client sending a {@link ServerboundRequestVeinMine}.
 */
public final class ClientboundVeinMineResults implements Message<VeinMinerClientboundMessageListener> {

    private final List<BlockPosition> blockPositions;

    /**
     * Construct a new {@link ClientboundVeinMineResults}.
     *
     * @param blockPositions the calculated {@link BlockPosition BlockPositions}
     */
    public ClientboundVeinMineResults(@NotNull Collection<BlockPosition> blockPositions) {
        this.blockPositions = new ArrayList<>(blockPositions);
    }

    /**
     * Construct a new {@link ClientboundVeinMineResults} with no positions.
     */
    public ClientboundVeinMineResults() {
        this(Collections.emptyList());
    }

    /**
     * Construct a new {@link ClientboundVeinMineResults} with input.
     *
     * @param buffer the input buffer
     */
    @Internal
    public ClientboundVeinMineResults(@NotNull MessageByteBuffer buffer) {
        BlockPosition[] blockPositions = new BlockPosition[buffer.readVarInt()];

        for (int i = 0; i < blockPositions.length; i++) {
            blockPositions[i] = buffer.read(BlockPosition.class);
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
    public void write(@NotNull MessageByteBuffer buffer) {
        buffer.writeVarInt(blockPositions.size());
        this.blockPositions.forEach(buffer::write);
    }

    @Override
    public void handle(@NotNull VeinMinerClientboundMessageListener listener) {
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
