package wtf.choco.veinminer.network.protocol.clientbound;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.network.PluginMessage;
import wtf.choco.veinminer.network.PluginMessageByteBuffer;
import wtf.choco.veinminer.network.protocol.ClientboundPluginMessageListener;
import wtf.choco.veinminer.util.BlockPosition;

public final class PluginMessageClientboundVeinMineResults implements PluginMessage<ClientboundPluginMessageListener> {

    private final Collection<? extends BlockPosition> blockPositions;

    public PluginMessageClientboundVeinMineResults(@NotNull Collection<? extends BlockPosition> blockPositions) {
        this.blockPositions = blockPositions;
    }

    public PluginMessageClientboundVeinMineResults() {
        this(Collections.emptyList());
    }

    public PluginMessageClientboundVeinMineResults(@NotNull PluginMessageByteBuffer buffer) {
        BlockPosition[] blockPositions = new BlockPosition[buffer.readVarInt()];

        for (int i = 0; i < blockPositions.length; i++) {
            blockPositions[i] = buffer.readBlockPosition();
        }

        this.blockPositions = Arrays.asList(blockPositions);
    }

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
