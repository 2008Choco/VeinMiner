package wtf.choco.veinminer;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import wtf.choco.veinminer.network.FabricChannelRegistrar;
import wtf.choco.veinminer.network.FabricServerState;
import wtf.choco.veinminer.network.protocol.serverbound.PluginMessageServerboundHandshake;
import wtf.choco.veinminer.network.protocol.serverbound.PluginMessageServerboundRequestVeinMine;
import wtf.choco.veinminer.network.protocol.serverbound.PluginMessageServerboundToggleVeinMiner;
import wtf.choco.veinminer.platform.FabricPlatformReconstructor;

public final class VeinMinerMod implements ClientModInitializer {

    private static final Identifier TEXTURE_VEINMINER_ICON = new Identifier("veinminer4bukkit", "textures/gui/veinminer_icon.png");

    private static FabricServerState serverState;

    private boolean veinminerActivated = false;

    @Override
    public void onInitializeClient() {
        VeinMiner veinminer = VeinMiner.getInstance();
        veinminer.setPlatformReconstructor(FabricPlatformReconstructor.INSTANCE);

        VeinMiner.PROTOCOL.registerChannels(new FabricChannelRegistrar());

        KeyBinding activationBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.veinminer4bukkit.activate_veinminer",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_GRAVE_ACCENT, // Tilde ~
            "category.veinminer4bukkit.general"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!hasServerState() || !getServerState().isEnabled()) {
                return;
            }

            boolean last = veinminerActivated;
            this.veinminerActivated = activationBinding.isPressed();

            if (last ^ veinminerActivated) {
                VeinMiner.PROTOCOL.sendMessageToServer(serverState, new PluginMessageServerboundToggleVeinMiner(veinminerActivated));

                if (veinminerActivated) {
                    VeinMiner.PROTOCOL.sendMessageToServer(serverState, new PluginMessageServerboundRequestVeinMine());
                }
            }
        });

        ClientPlayConnectionEvents.INIT.register((handler, client) -> {
            // Initialize a new server state
            serverState = new FabricServerState(client);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            serverState = null; // Nullify the server state. We are no longer connected
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            // Once joined, we're going to send a handshake packet to let the server know we have the client mod installed
            VeinMiner.PROTOCOL.sendMessageToServer(serverState, new PluginMessageServerboundHandshake(VeinMiner.PROTOCOL.getVersion()));
        });

        HudRenderCallback.EVENT.register((stack, delta) -> {
            if (!hasServerState() || !getServerState().isEnabled() || !veinminerActivated || !MinecraftClient.isHudEnabled()) {
                return;
            }

            MinecraftClient client = MinecraftClient.getInstance();
            Window window = client.getWindow();
            int width = window.getScaledWidth(), height = window.getScaledHeight();

            stack.push();

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, TEXTURE_VEINMINER_ICON);
            DrawableHelper.drawTexture(stack, (width / 2) + 8, (height / 2) - 4, 0, 0, 8, 8, 8, 8);

            stack.pop();
        });
    }

    /**
     * Check whether or not the client is connected to a server and has a valid server state.
     * <p>
     * The result of this method should be checked before accessing {@link #getServerState()}.
     *
     * @return true if a server state is available, false otherwise
     */
    public static boolean hasServerState() {
        return serverState != null;
    }

    /**
     * Get the {@link FabricServerState} for this client.
     *
     * @return the server state
     *
     * @throws IllegalStateException if there is no server state
     */
    @NotNull
    public static FabricServerState getServerState() {
        if (!hasServerState()) {
            throw new IllegalStateException("Tried to get FabricServerState while not connected to server.");
        }

        return serverState;
    }

}
