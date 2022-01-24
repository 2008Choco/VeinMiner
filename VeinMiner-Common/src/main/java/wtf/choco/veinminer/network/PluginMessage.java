package wtf.choco.veinminer.network;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a message sent between client and server.
 *
 * @param <T> the type of message listener
 */
public interface PluginMessage<T extends PluginMessageListener> {

    /**
     * Write this plugin message to the provided byte buffer.
     *
     * @param buffer the buffer to which data should be written
     */
    public void write(@NotNull PluginMessageByteBuffer buffer);

    /**
     * Handle this message.
     *
     * @param listener the plugin message listener
     */
    public void handle(@NotNull T listener);

}
