package wtf.choco.veinminer.network.message;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.api.ClientActivation;
import wtf.choco.veinminer.network.PluginMessage;
import wtf.choco.veinminer.network.PluginMessageByteBuffer;

/**
 * A serverbound {@link PluginMessage} including the following data:
 * <ol>
 *   <li><strong>varint</strong>: protocol version
 * </ol>
 * Sent when the client joins the server.
 */
public class PluginMessageInHandshake implements PluginMessage<@NotNull VeinMiner> {

    private int protocolVersion;

    @Override
    public void read(@NotNull PluginMessageByteBuffer buffer) {
        this.protocolVersion = buffer.readVarInt();
    }

    @Override
    public void write(@NotNull PluginMessageByteBuffer buffer) {
        buffer.writeVarInt(protocolVersion);
    }

    @Override
    public void handle(@NotNull VeinMiner plugin, @NotNull Player player) {
        int serverProtocolVersion = plugin.getPluginMessageProtocol().getVersion();
        if (serverProtocolVersion == protocolVersion) {
            ClientActivation.setUsingClientMod(player, true);
            return;
        }

        player.kickPlayer("Your client-side version of VeinMiner (for Bukkit) is " + (serverProtocolVersion > protocolVersion ? "out of date. Please update." : "too new. Please downgrade."));
    }

}
