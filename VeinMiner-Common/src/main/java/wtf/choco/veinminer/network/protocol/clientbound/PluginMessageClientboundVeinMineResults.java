package wtf.choco.veinminer.network.protocol.clientbound;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

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

    private final Collection<? extends BlockPosition> blockPositions;

    /**
     * Construct a new {@link PluginMessageClientboundVeinMineResults}.
     *
     * @param blockPositions the calculated {@link BlockPosition BlockPositions}
     */
    public PluginMessageClientboundVeinMineResults(@NotNull Collection<? extends BlockPosition> blockPositions) {
        this.blockPositions = blockPositions;
    }

    /**
     * Construct a new {@link PluginMessageClientboundVeinMineResults} with no positions.
     */
    public PluginMessageClientboundVeinMineResults() {
        this(Collections.emptyList());
    }

    @Internal
    public PluginMessageClientboundVeinMineResults(@NotNull PluginMessageByteBuffer buffer) {
        BlockPosition[] blockPositions = new BlockPosition[buffer.readVarInt()];

        for (int i = 0; i < blockPositions.length; i++) {
            blockPositions[i] = buffer.readBlockPosition();
        }

        this.blockPositions = Arrays.asList(blockPositions);
    }

    /**
     * Get a {@link Collection} of all {@link BlockPosition BlockPositions} resulting from
     * the vein mine. May be empty if the vein mine was unsuccessful.
     *
     * @return the block positions
     */
    public Collection<? extends BlockPosition> getBlockPositions() {
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

}
