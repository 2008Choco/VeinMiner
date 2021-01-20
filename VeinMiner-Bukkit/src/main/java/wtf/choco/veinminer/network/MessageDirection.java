package wtf.choco.veinminer.network;

public enum MessageDirection {

    CLIENTBOUND,
    SERVERBOUND;


    public boolean isClientbound() {
        return this == CLIENTBOUND;
    }

    public boolean isServerbound() {
        return this == SERVERBOUND;
    }

}
