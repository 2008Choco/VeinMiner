package wtf.choco.veinminer.network.message;

import org.bukkit.entity.Player;

import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.network.PluginMessage;
import wtf.choco.veinminer.network.PluginMessageByteBuffer;

public class PluginMessageInHandshake implements PluginMessage<VeinMiner> {

    private int protocolVersion;

    @Override
    public void read(PluginMessageByteBuffer buffer) {
        this.protocolVersion = buffer.readVarInt();
    }

    @Override
    public void write(PluginMessageByteBuffer buffer) {
        buffer.writeVarInt(protocolVersion);
    }

    @Override
    public void handle(VeinMiner plugin, Player player) {
        int serverProtocolVersion = plugin.getPluginMessageProtocol().getVersion();
        if (serverProtocolVersion == protocolVersion) {
            return;
        }

        player.kickPlayer("Your client-side version of VeinMiner (for Bukkit) is " + (serverProtocolVersion > protocolVersion ? "out of date. Please update." : "too new. Please downgrade."));
    }

}
