package wtf.choco.veinminer.network;

import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import wtf.choco.network.Message;
import wtf.choco.network.fabric.FabricChannelRegistrar;
import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.VeinMinerMod;
import wtf.choco.veinminer.network.protocol.VeinMinerClientboundMessageListener;
import wtf.choco.veinminer.network.protocol.VeinMinerServerboundMessageListener;

public final class VeinMinerFabricChannelRegistrar extends FabricChannelRegistrar<VeinMinerServerboundMessageListener, VeinMinerClientboundMessageListener> {

    public VeinMinerFabricChannelRegistrar(Logger logger) {
        super(VeinMiner.PROTOCOL, logger, true);
    }

    @Nullable
    @Override
    protected VeinMinerClientboundMessageListener onSuccessfulClientboundMessage(@NotNull ResourceLocation channel, @NotNull Message<VeinMinerClientboundMessageListener> message) {
        return VeinMinerMod.hasServerState() ? VeinMinerMod.getServerState() : null;
    }

}
