package wtf.choco.veinminer.fabric.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.fabric.VeinMinerMod;
import wtf.choco.veinminer.network.ChannelRegistrar;
import wtf.choco.veinminer.network.PluginMessage;
import wtf.choco.veinminer.network.PluginMessageByteBuffer;
import wtf.choco.veinminer.network.PluginMessageRegistry;
import wtf.choco.veinminer.network.protocol.ClientboundPluginMessageListener;
import wtf.choco.veinminer.network.protocol.ServerboundPluginMessageListener;
import wtf.choco.veinminer.util.NamespacedKey;

public final class FabricChannelRegistrar implements ChannelRegistrar {

    @Override
    public void registerClientboundMessageHandler(@NotNull NamespacedKey channel, @NotNull PluginMessageRegistry<ClientboundPluginMessageListener> registry) {
        ClientPlayNetworking.registerGlobalReceiver(new Identifier(channel.namespace(), channel.key()), (client, handler, buf, responseSender) -> {
            PluginMessageByteBuffer buffer = new PluginMessageByteBuffer(buf.array());

            int messageId = buffer.readVarInt();
            PluginMessage<ClientboundPluginMessageListener> message = registry.createPluginMessage(messageId, buffer);

            // Ignore any unknown messages
            if (message == null || !VeinMinerMod.hasServerState()) {
                return;
            }

            message.handle(VeinMinerMod.getServerState());
        });
    }

    @Override // Outgoing channels need not be registered on Fabric. We can disregard this
    public void registerServerboundMessageHandler(@NotNull NamespacedKey channel, @NotNull PluginMessageRegistry<ServerboundPluginMessageListener> registry) { }

}
