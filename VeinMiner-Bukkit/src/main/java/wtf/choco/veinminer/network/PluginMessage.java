package wtf.choco.veinminer.network;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public interface PluginMessage<T extends Plugin> {

    public void read(PluginMessageByteBuffer buffer);

    public void write(PluginMessageByteBuffer buffer);

    public void handle(T plugin, Player player);

}
