package wtf.choco.veinminer.network;

/**
 * A direction in which a {@link PluginMessage} may be sent over a protocol.
 *
 * @see PluginMessage
 * @see PluginMessageProtocol
 */
public enum MessageDirection {

    /**
     * A message sent from server to the client.
     */
    CLIENTBOUND,

    /**
     * A message sent from the client to the server.
     */
    SERVERBOUND;


    /**
     * A utility method equivalent to {@code this == CLIENTBOUND}.
     *
     * @return true if the direction is clientbound
     */
    public boolean isClientbound() {
        return this == CLIENTBOUND;
    }

    /**
     * A utility method equivalent to {@code this == SERVERBOUND}.
     *
     * @return true if the direction is serverbound
     */
    public boolean isServerbound() {
        return this == SERVERBOUND;
    }

}
