package wtf.choco.veinminer.network;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a message sent between client and server.
 *
 * @param <T> the plugin to which this message belongs
 */
public interface PluginMessage<T extends Plugin> {

    /**
     * Read from the provided byte buffer to this plugin message's fields.
     *
     * @param buffer the buffer from which to read
     */
    public void read(@NotNull PluginMessageByteBuffer buffer);

    /**
     * Write this plugin message to the provided byte buffer.
     *
     * @param buffer the buffer to which data should be written
     */
    public void write(@NotNull PluginMessageByteBuffer buffer);

    /**
     * Handle the incoming of this message.
     *
     * @param plugin the plugin that received this message.
     * @param player the player that sent the message
     */
    public void handle(@NotNull T plugin, @NotNull Player player);

}
