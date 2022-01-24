package wtf.choco.veinminer.fabric.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.network.MessageReceiver;
import wtf.choco.veinminer.network.protocol.ClientboundPluginMessageListener;
import wtf.choco.veinminer.util.NamespacedKey;

public final class FabricServerState implements ClientboundPluginMessageListener, MessageReceiver {

    @Override
    public void sendMessage(@NotNull NamespacedKey channel, byte[] message) {
        PacketByteBuf byteBuf = PacketByteBufs.create();
        byteBuf.writeBytes(message);

        ClientPlayNetworking.send(new Identifier(channel.namespace(), channel.key()), byteBuf);
    }

}
