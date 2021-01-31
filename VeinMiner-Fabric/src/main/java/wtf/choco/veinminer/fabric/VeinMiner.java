package wtf.choco.veinminer.fabric;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import org.lwjgl.glfw.GLFW;

public class VeinMiner implements ClientModInitializer {

    private static final Identifier TEXTURE_VEINMINER_ICON = new Identifier("veinminer4bukkit", "textures/gui/veinminer_icon.png");

    private boolean veinminerActivated = false;

    @Override
    public void onInitializeClient() {
        KeyBinding activationBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.veinminer4bukkit.activate_veinminer",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_GRAVE_ACCENT, // Tilde ~
            "category.veinminer4bukkit.general"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean last = veinminerActivated;
            this.veinminerActivated = activationBinding.isPressed();

            if (last ^ veinminerActivated) {
                PacketByteBuf buffer = PacketByteBufs.create();
                buffer.writeVarInt(VeinMinerBukkitProtocol.OUT_TOGGLE_VEINMINER);
                buffer.writeBoolean(veinminerActivated);
                ClientPlayNetworking.send(VeinMinerBukkitProtocol.CHANNEL_IDENTIFIER, buffer);
            }
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            PacketByteBuf buffer = PacketByteBufs.create();
            buffer.writeVarInt(VeinMinerBukkitProtocol.OUT_HANDSHAKE);
            buffer.writeVarInt(VeinMinerBukkitProtocol.VEINMINER_PROTOCOL_VERSION);
            ClientPlayNetworking.send(VeinMinerBukkitProtocol.CHANNEL_IDENTIFIER, buffer);
        });

        ClientPlayNetworking.registerGlobalReceiver(VeinMinerBukkitProtocol.CHANNEL_IDENTIFIER, (client, handler, buf, responseSender) -> {
            // For when messages are sent to the client... not yet though.
        });

        HudRenderCallback.EVENT.register((stack, delta) -> {
            if (!veinminerActivated || !MinecraftClient.isHudEnabled()) {
                return;
            }

            MinecraftClient client = MinecraftClient.getInstance();
            Window window = client.getWindow();
            int width = window.getScaledWidth(), height = window.getScaledHeight();

            RenderSystem.pushMatrix();

            client.getTextureManager().bindTexture(TEXTURE_VEINMINER_ICON);
            DrawableHelper.drawTexture(stack, (width / 2) + 8, (height / 2) - 4, 0, 0, 8, 8, 8, 8);

            RenderSystem.popMatrix();
        });
    }

}
