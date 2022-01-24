package wtf.choco.veinminer.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.network.protocol.ClientboundPluginMessageListener;
import wtf.choco.veinminer.network.protocol.clientbound.PluginMessageClientboundHandshakeResponse;
import wtf.choco.veinminer.util.NamespacedKey;

public final class FabricServerState implements ClientboundPluginMessageListener, MessageReceiver {

    private boolean enabled;

    public FabricServerState(MinecraftClient client) {
        // We'll enable VeinMiner if we're in singleplayer development mode, just for testing
        this.enabled = client.isInSingleplayer() && FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void sendMessage(@NotNull NamespacedKey channel, byte[] message) {
        PacketByteBuf byteBuf = PacketByteBufs.create();
        byteBuf.writeBytes(message);

        ClientPlayNetworking.send(new Identifier(channel.namespace(), channel.key()), byteBuf);
    }

    @Override
    public void handleClientboundHandshakeResponse(PluginMessageClientboundHandshakeResponse message) {
        this.enabled = message.isEnabled();
    }

}
