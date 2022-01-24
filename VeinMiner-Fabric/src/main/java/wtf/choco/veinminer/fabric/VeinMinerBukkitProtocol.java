package wtf.choco.veinminer.fabric;

import net.minecraft.util.Identifier;

public final class VeinMinerBukkitProtocol {

    public static final int VEINMINER_PROTOCOL_VERSION = 1;
    public static final Identifier CHANNEL_IDENTIFIER = new Identifier("veinminer", "veinminer");

    public static final int OUT_HANDSHAKE = 0x00;
    public static final int OUT_TOGGLE_VEINMINER = 0x01;

    private VeinMinerBukkitProtocol() { }

}
