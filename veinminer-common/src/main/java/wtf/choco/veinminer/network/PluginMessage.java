package wtf.choco.veinminer.network;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a message sent between client and server.
 * <p>
 * By convention, a PluginMessage implementation should have a constructor that accepts a
 * {@link PluginMessageByteBuffer} from which data may be read into final fields. An example
 * implementation may look something like the following:
 * <pre>
 * public final class PluginMessageServerboundExample implements PluginMessage{@literal <ServerboundPluginMessageListener>} {
 *
 *     private final String stringValue;
 *     private final int intValue;
 *
 *     // This is intended for when the message needs to be constructed to be sent to the client/server
 *     public PluginMessageServerboundExample(String stringValue, int intValue) {
 *         this.stringValue = stringValue;
 *         this.intValue = intValue;
 *     }
 *
 *     // This is intended for reading from the byte buffer. Used on construction of this message.
 *     public PluginMessageServerboundExample(PluginMessageByteBuffer buffer) {
 *         this(buffer.readString(), buffer.readVarInt());
 *     }
 *
 *     public String getStringValue() {
 *         return stringValue;
 *     }
 *
 *     public int getIntValue() {
 *         return intValue;
 *     }
 *
 *     {@literal @Override}
 *     public void write(PluginMessageByteBuffer buffer) {
 *         buffer.writeString(stringValue);
 *         buffer.writeVarInt(intValue);
 *     }
 *
 *     {@literal @Override}
 *     public void handle(ServerboundPluginMessageListener listener) {
 *         // Handle here. Conventionally, the listener should have a method to handle this message in specific... like so:
 *         listener.handleExample(this);
 *     }
 *
 * }
 * </pre>
 *
 * @param <T> the type of listener that will handle this message
 */
public interface PluginMessage<T extends PluginMessageListener> {

    /**
     * Write this plugin message to the provided {@link PluginMessageByteBuffer}.
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
