package wtf.choco.veinminer.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import wtf.choco.network.ChannelRegistrar;
import wtf.choco.network.Message;
import wtf.choco.network.MessageByteBuffer;
import wtf.choco.network.MessageRegistry;
import wtf.choco.network.data.NamespacedKey;
import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.VeinMinerMod;
import wtf.choco.veinminer.network.protocol.VeinMinerClientboundMessageListener;
import wtf.choco.veinminer.network.protocol.VeinMinerServerboundMessageListener;

/**
 * A Fabric implementation of {@link ChannelRegistrar}.
 */
public final class FabricChannelRegistrar implements ChannelRegistrar<VeinMinerServerboundMessageListener, VeinMinerClientboundMessageListener> {

    // TODO: Make a networking-fabric module under networking to handle some of this automatically

    @Override
    public void registerClientboundMessageHandler(@NotNull NamespacedKey channel, @NotNull MessageRegistry<VeinMinerClientboundMessageListener> registry) {
        ClientPlayNetworking.registerGlobalReceiver(new ResourceLocation(channel.namespace(), channel.key()), (client, handler, buf, responseSender) -> {
            MessageByteBuffer buffer = new MessageByteBuffer(VeinMiner.PROTOCOL, buf.nioBuffer());

            int messageId = buffer.readVarInt();
            Message<VeinMinerClientboundMessageListener> message = registry.createMessage(messageId, buffer);

            // Ignore any unknown messages
            if (message == null || !VeinMinerMod.hasServerState()) {
                return;
            }

            message.handle(VeinMinerMod.getServerState());
        });
    }

    @Override // Outgoing channels need not be registered on Fabric. We can disregard this
    public void registerServerboundMessageHandler(@NotNull NamespacedKey channel, @NotNull MessageRegistry<VeinMinerServerboundMessageListener> registry) {

    }

}
