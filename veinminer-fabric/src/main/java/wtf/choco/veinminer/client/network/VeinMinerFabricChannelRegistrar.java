package wtf.choco.veinminer.client.network;

import net.minecraft.resources.Identifier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import wtf.choco.network.Message;
import wtf.choco.network.fabric.FabricClientChannelRegistrar;
import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.client.VeinMinerClient;
import wtf.choco.veinminer.network.protocol.VeinMinerClientboundMessageListener;
import wtf.choco.veinminer.network.protocol.VeinMinerServerboundMessageListener;

public final class VeinMinerFabricChannelRegistrar extends FabricClientChannelRegistrar<VeinMinerServerboundMessageListener, VeinMinerClientboundMessageListener> {

    private final VeinMinerClient client;

    public VeinMinerFabricChannelRegistrar(@NotNull VeinMinerClient client, @NotNull Logger logger) {
        super(VeinMiner.PROTOCOL, logger);
        this.client = client;
    }

    @Nullable
    @Override
    protected VeinMinerClientboundMessageListener onSuccessfulMessage(@NotNull Identifier channel, @NotNull Message<VeinMinerClientboundMessageListener> message) {
        return client.hasServerState() ? client.getServerState() : null;
    }

}
