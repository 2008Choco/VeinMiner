package wtf.choco.veinminer.network;

import org.jetbrains.annotations.NotNull;

import wtf.choco.network.MessageProtocol;
import wtf.choco.network.ProtocolConfiguration;
import wtf.choco.veinminer.util.BlockPosition;

public final class VeinMinerProtocolConfiguration implements ProtocolConfiguration {

    @Override
    public void configure(@NotNull MessageProtocol<?, ?> protocol) {
        protocol.registerCustomDataType(BlockPosition.class, BlockPosition::read);
    }

}
