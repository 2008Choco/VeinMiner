package wtf.choco.veinminer.network.protocol.serverbound;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import wtf.choco.network.Message;
import wtf.choco.network.MessageByteBuffer;
import wtf.choco.veinminer.documentation.Documentation;
import wtf.choco.veinminer.documentation.MessageField;
import wtf.choco.veinminer.documentation.ProtocolMessageDocumentation;
import wtf.choco.veinminer.network.protocol.VeinMinerServerboundMessageListener;

/**
 * A server bound {@link Message} including the following data:
 * <ol>
 *   <li><strong>boolean</strong>: whether or not vein miner is active
 * </ol>
 * Sent when a client presses or releases the vein miner activation key bind.
 */
public final class ServerboundToggleVeinMiner implements Message<VeinMinerServerboundMessageListener> {

    private final boolean activated;

    /**
     * Construct a new {@link ServerboundToggleVeinMiner}.
     *
     * @param activated whether or not vein miner has been activated
     */
    public ServerboundToggleVeinMiner(boolean activated) {
        this.activated = activated;
    }

    /**
     * Construct a new {@link ServerboundToggleVeinMiner} with input.
     *
     * @param buffer the input buffer
     */
    @Internal
    public ServerboundToggleVeinMiner(@NotNull MessageByteBuffer buffer) {
        this.activated = buffer.readBoolean();
    }

    /**
     * Check whether or not vein miner is activated.
     *
     * @return true if activated, false if deactivated
     */
    public boolean isActivated() {
        return activated;
    }

    @Override
    public void write(@NotNull MessageByteBuffer buffer) {
        buffer.writeBoolean(activated);
    }

    @Override
    public void handle(@NotNull VeinMinerServerboundMessageListener listener) {
        listener.handleToggleVeinMiner(this);
    }

    @Documentation
    private static void document(ProtocolMessageDocumentation.Builder documentation) {
        documentation.name("Toggle Vein Miner")
            .description("""
                    Sent by the client to inform the server that it has activated or deactivated its vein miner keybind.
                    """)
            .field(MessageField.TYPE_BOOLEAN, "State", "The new state of the vein miner activation");
    }

}
