package wtf.choco.veinminer.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;

import org.lwjgl.glfw.GLFW;

public class VeinMiner implements ClientModInitializer {

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
                buffer.writeVarInt(VeinMinerBukkitProtocol.TOGGLE_VEINMINER);
                buffer.writeBoolean(veinminerActivated);
                ClientPlayNetworking.send(VeinMinerBukkitProtocol.CHANNEL_IDENTIFIER, buffer);
            }
        });
    }

}
