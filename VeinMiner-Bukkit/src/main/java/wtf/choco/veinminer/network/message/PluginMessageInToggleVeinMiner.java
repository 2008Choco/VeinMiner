package wtf.choco.veinminer.network.message;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.api.ClientActivation;
import wtf.choco.veinminer.network.PluginMessage;
import wtf.choco.veinminer.network.PluginMessageByteBuffer;
import wtf.choco.veinminer.utils.VMConstants;
import wtf.choco.veinminer.utils.VMEventFactory;

/**
 * A serverbound {@link PluginMessage} including the following data:
 * <ol>
 *   <li><strong>boolean</strong>: whether or not vein miner is active
 * </ol>
 * Sent when the client presses or releases the vein miner activating keybind.
 */
public class PluginMessageInToggleVeinMiner implements PluginMessage<@NotNull VeinMiner> {

    private boolean activated;

    @Override
    public void read(@NotNull PluginMessageByteBuffer buffer) {
        this.activated = buffer.readBoolean();
    }

    @Override
    public void write(@NotNull PluginMessageByteBuffer buffer) {
        buffer.writeBoolean(activated);
    }

    @Override
    public void handle(@NotNull VeinMiner plugin, @NotNull Player player) {
        if (!plugin.getConfig().getBoolean(VMConstants.CONFIG_CLIENT_ALLOW_CLIENT_ACTIVATION, true)) {
            return;
        }

        if (!VMEventFactory.handlePlayerClientActivateVeinMinerEvent(player, activated)) {
            return;
        }

        ClientActivation.setActivatedOnClient(player, activated);
    }

}
