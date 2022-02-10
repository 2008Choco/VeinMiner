package wtf.choco.veinminer;

import java.util.Objects;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import wtf.choco.veinminer.hud.HudRenderComponent;
import wtf.choco.veinminer.hud.HudRenderComponentPatternWheel;
import wtf.choco.veinminer.hud.HudRenderComponentVeinMiningIcon;
import wtf.choco.veinminer.network.FabricChannelRegistrar;
import wtf.choco.veinminer.network.FabricServerState;
import wtf.choco.veinminer.network.protocol.serverbound.PluginMessageServerboundHandshake;
import wtf.choco.veinminer.network.protocol.serverbound.PluginMessageServerboundRequestVeinMine;
import wtf.choco.veinminer.network.protocol.serverbound.PluginMessageServerboundToggleVeinMiner;
import wtf.choco.veinminer.platform.FabricPlatformReconstructor;

/**
 * The Fabric VeinMiner mod entry class.
 */
public final class VeinMinerMod implements ClientModInitializer {

    public static final KeyBinding KEY_BINDING_ACTIVATE_VEINMINER = registerKeybind("activate_veinminer", GLFW.GLFW_KEY_GRAVE_ACCENT);
    public static final KeyBinding KEY_BINDING_NEXT_PATTERN = registerKeybind("next_pattern", GLFW.GLFW_KEY_RIGHT_BRACKET);
    public static final KeyBinding KEY_BINDING_PREVIOUS_PATTERN = registerKeybind("previous_pattern", GLFW.GLFW_KEY_LEFT_BRACKET);

    private static FabricServerState serverState;

    private final HudRenderComponentPatternWheel patternWheelRenderComponent = new HudRenderComponentPatternWheel();
    private final HudRenderComponent[] hudRenderComponents = {
            new HudRenderComponentVeinMiningIcon(),
            patternWheelRenderComponent
    };

    private boolean changingPatterns = false;
    private BlockPos lastTargetBlockPosition = null;

    @Override
    public void onInitializeClient() {
        VeinMiner veinminer = VeinMiner.getInstance();
        veinminer.setPlatformReconstructor(FabricPlatformReconstructor.INSTANCE);

        VeinMiner.PROTOCOL.registerChannels(new FabricChannelRegistrar());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!hasServerState() || !getServerState().isEnabled()) {
                return;
            }

            boolean lastActive = getServerState().isActive(), active = KEY_BINDING_ACTIVATE_VEINMINER.isPressed();
            getServerState().setActive(active);

            // Activating / deactivating vein miner
            if (lastActive ^ active) {
                VeinMiner.PROTOCOL.sendMessageToServer(serverState, new PluginMessageServerboundToggleVeinMiner(active));

                if (active) {
                    VeinMiner.PROTOCOL.sendMessageToServer(serverState, new PluginMessageServerboundRequestVeinMine());
                }
            }

            // Requesting that the server vein mine at the player's current target block because it's different
            HitResult result = client.crosshairTarget;
            if (active && result instanceof BlockHitResult blockResult && !Objects.equals(lastTargetBlockPosition, blockResult.getBlockPos())) {
                VeinMiner.PROTOCOL.sendMessageToServer(serverState, new PluginMessageServerboundRequestVeinMine());
            }

            this.lastTargetBlockPosition = (result instanceof BlockHitResult blockResult) ? blockResult.getBlockPos() : null;

            // Changing patterns
            boolean lastChangingPatterns = changingPatterns;
            this.changingPatterns = (KEY_BINDING_NEXT_PATTERN.isPressed() || KEY_BINDING_PREVIOUS_PATTERN.isPressed());

            if (lastChangingPatterns ^ changingPatterns) {
                boolean next;

                // There has to be a smarter way to write this...
                if (KEY_BINDING_NEXT_PATTERN.isPressed()) {
                    next = true;
                }
                else if (KEY_BINDING_PREVIOUS_PATTERN.isPressed()) {
                    next = false;
                }
                else {
                    return;
                }

                // If the HUD wheel isn't rendered yet, push a render call but don't change the pattern
                if (patternWheelRenderComponent.shouldRender()) {
                    serverState.changePattern(next);
                }

                this.patternWheelRenderComponent.pushRender();
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

        HudRenderCallback.EVENT.register((stack, tickDelta) -> {
            if (!hasServerState() || !getServerState().isEnabled()) {
                return;
            }

            MinecraftClient client = MinecraftClient.getInstance();

            for (HudRenderComponent component : hudRenderComponents) {
                if (!component.shouldRender()) {
                    continue;
                }

                component.render(client, stack, tickDelta);
            }
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

    private static KeyBinding registerKeybind(String id, int key) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.veinminer4bukkit." + id,
            InputUtil.Type.KEYSYM,
            key,
            "category.veinminer4bukkit.general"
        ));
    }

}
