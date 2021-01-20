package wtf.choco.veinminer.network.message;

import org.bukkit.entity.Player;

import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.api.ClientActivation;
import wtf.choco.veinminer.network.PluginMessage;
import wtf.choco.veinminer.network.PluginMessageByteBuffer;

/**
 * A serverbound {@link PluginMessage} including the following data:
 * <ol>
 *   <li><strong>boolean</strong>: whether or not vein miner is active
 * </ol>
 * Sent when the client presses or releases the vein miner activating keybind.
 */
public class PluginMessageInToggleVeinMiner implements PluginMessage<VeinMiner> {

    private boolean activated;

    @Override
    public void read(PluginMessageByteBuffer buffer) {
        this.activated = buffer.readBoolean();
    }

    @Override
    public void write(PluginMessageByteBuffer buffer) {
        buffer.writeBoolean(activated);
    }

    @Override
    public void handle(VeinMiner plugin, Player player) {
        ClientActivation.setActivatedOnClient(player, activated);
    }

}
