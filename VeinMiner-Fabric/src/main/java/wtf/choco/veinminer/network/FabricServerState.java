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

/**
 * The client's state on a connected server.
 */
public final class FabricServerState implements ClientboundPluginMessageListener, MessageReceiver {

    private boolean enabled;

    /**
     * Construct a new {@link FabricServerState}.
     *
     * @param client the {@link MinecraftClient} instance
     */
    public FabricServerState(MinecraftClient client) {
        // We'll enable VeinMiner if we're in singleplayer development mode, just for testing
        this.enabled = client.isInSingleplayer() && FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    /**
     * Check whether or not vein miner is enabled on the client.
     * <p>
     * If this method returns {@code false}, this means that the server has not allowed the
     * client to activate vein miner using a key bind, and therefore the client should not be
     * sending messages to the server claiming that it has been activated or deactivated, or
     * perform any other client-sided functionality.
     *
     * @return true if enabled, false otherwise
     */
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
    public void handleClientboundHandshakeResponse(@NotNull PluginMessageClientboundHandshakeResponse message) {
        this.enabled = message.isEnabled();
    }

}
