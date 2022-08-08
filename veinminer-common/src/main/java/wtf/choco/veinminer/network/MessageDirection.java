package wtf.choco.veinminer.network;

/**
 * A direction in which a {@link PluginMessage} may be sent over the protocol.
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
     * A convenience method equivalent to {@code this == CLIENTBOUND}.
     *
     * @return true if the direction is client bound
     */
    public boolean isClientbound() {
        return this == CLIENTBOUND;
    }

    /**
     * A convenience method equivalent to {@code this == SERVERBOUND}.
     *
     * @return true if the direction is server bound
     */
    public boolean isServerbound() {
        return this == SERVERBOUND;
    }

}
