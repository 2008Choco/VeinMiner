package wtf.choco.veinminer.network;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.util.NamespacedKey;

/**
 * A target capable of being sent a plugin message.
 */
public interface MessageReceiver {

    /**
     * Send a message represented by the given bytes on the specified channel.
     *
     * @param channel the channel on which the message should be sent
     * @param message the message bytes to be sent
     */
    public void sendMessage(@NotNull NamespacedKey channel, byte[] message);

}
