package wtf.choco.veinminer;

import java.util.Objects;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import wtf.choco.veinminer.config.ClientConfig;
import wtf.choco.veinminer.hud.HudRenderComponent;
import wtf.choco.veinminer.hud.HudRenderComponentPatternWheel;
import wtf.choco.veinminer.hud.HudRenderComponentVeinMiningIcon;
import wtf.choco.veinminer.network.FabricChannelRegistrar;
import wtf.choco.veinminer.network.FabricServerState;
import wtf.choco.veinminer.network.protocol.serverbound.PluginMessageServerboundHandshake;
import wtf.choco.veinminer.network.protocol.serverbound.PluginMessageServerboundRequestVeinMine;
import wtf.choco.veinminer.network.protocol.serverbound.PluginMessageServerboundToggleVeinMiner;
import wtf.choco.veinminer.platform.FabricPlatformReconstructor;
import wtf.choco.veinminer.render.VeinMinerRenderLayer;

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

    @Override
    public void onInitializeClient() {
        VeinMiner veinminer = VeinMiner.getInstance();
        veinminer.setPlatformReconstructor(FabricPlatformReconstructor.INSTANCE);

        VeinMiner.PROTOCOL.registerChannels(new FabricChannelRegistrar());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!hasServerState() || !getServerState().isEnabledOnServer()) {
                return;
            }

            ClientConfig config = getServerState().getConfig();

            boolean shouldRequestVeinMine = false;
            boolean active = KEY_BINDING_ACTIVATE_VEINMINER.isPressed();

            if (config.isAllowActivationKeybind()) {
                boolean lastActive = getServerState().isActive();
                getServerState().setActive(active = KEY_BINDING_ACTIVATE_VEINMINER.isPressed());

                // Activating / deactivating vein miner
                if (lastActive ^ active) {
                    VeinMiner.PROTOCOL.sendMessageToServer(serverState, new PluginMessageServerboundToggleVeinMiner(active));
                    shouldRequestVeinMine = active;
                }
            }

            HitResult result = client.crosshairTarget;

            if (result instanceof BlockHitResult blockResult) {
                BlockPos position = blockResult.getBlockPos();
                Direction blockFace = blockResult.getSide();

                // Requesting that the server vein mine at the player's current target block because it's different
                shouldRequestVeinMine |= (active && config.isAllowActivationKeybind() && (!Objects.equals(getServerState().getLastLookedAtBlockPos(), position) || !Objects.equals(getServerState().getLastLookedAtBlockFace(), blockFace)));

                if (shouldRequestVeinMine) {
                    getServerState().resetShape();
                    VeinMiner.PROTOCOL.sendMessageToServer(serverState, new PluginMessageServerboundRequestVeinMine(position.getX(), position.getY(), position.getZ()));
                }

                // Updating the new last looked at position
                if (client.player != null && client.player.world != null && !client.player.world.isAir(position)) {
                    getServerState().setLastLookedAt(position, blockFace);
                } else {
                    getServerState().setLastLookedAt(null, null);
                }
            }

            // Changing patterns
            if (config.isAllowPatternSwitchingKeybind()) {
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
                    if (patternWheelRenderComponent.shouldRender(config)) {
                        serverState.changePattern(next);
                    }

                    this.patternWheelRenderComponent.pushRender();
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

        HudRenderCallback.EVENT.register((stack, tickDelta) -> {
            if (!hasServerState() || !getServerState().isEnabledOnServer()) {
                return;
            }

            MinecraftClient client = MinecraftClient.getInstance();
            ClientConfig config = getServerState().getConfig();

            for (HudRenderComponent component : hudRenderComponents) {
                if (!component.shouldRender(config)) {
                    continue;
                }

                component.render(client, stack, tickDelta);
            }
        });

        /*
         * Massive credit to FTB-Ultimine for help with this rendering code. I don't think I
         * would have been able to figure this out myself... I'm not familiar with navigating the
         * Minecraft codebase. But this does make a lot of sense and it helped a lot. I'm very
         * appreciative for open source code :)
         *
         * https://github.com/FTBTeam/FTB-Ultimine
         */
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            if (!hasServerState() || !getServerState().isEnabledOnServer() || !getServerState().isActive() || !getServerState().getConfig().isAllowWireframeRendering()) {
                return;
            }

            BlockPos origin = getServerState().getLastLookedAtBlockPos();
            VoxelShape shape = getServerState().getVeinMineResultShape();

            if (origin == null || shape == null) {
                return;
            }

            MinecraftClient client = MinecraftClient.getInstance();

            // Calculate the stack
            MatrixStack stack = context.matrixStack();
            Vec3d projectedView = client.getEntityRenderDispatcher().camera.getPos();

            stack.push();
            stack.translate(origin.getX() - projectedView.x, origin.getY() - projectedView.y, origin.getZ() - projectedView.z);

            Matrix4f matrix = stack.peek().getPositionMatrix();

            // Buffer vertices
            VertexConsumerProvider.Immediate consumers = client.getBufferBuilders().getEntityVertexConsumers();

            // Full wireframe drawing
            VertexConsumer buffer = consumers.getBuffer(VeinMinerRenderLayer.getWireframe());

            shape.forEachEdge((x1, y1, z1, x2, y2, z2) -> {
                buffer.vertex(matrix, (float) x1, (float) y1, (float) z1).color(255, 255, 255, 255).next();
                buffer.vertex(matrix, (float) x2, (float) y2, (float) z2).color(255, 255, 255, 255).next();
            });

            consumers.draw(VeinMinerRenderLayer.getWireframe());

            // Transparent drawing
            VertexConsumer bufferTransparent = consumers.getBuffer(VeinMinerRenderLayer.getWireframeTransparent());

            shape.forEachEdge((x1, y1, z1, x2, y2, z2) -> {
                bufferTransparent.vertex(matrix, (float) x1, (float) y1, (float) z1).color(255, 255, 255, 64).next();
                bufferTransparent.vertex(matrix, (float) x2, (float) y2, (float) z2).color(255, 255, 255, 64).next();
            });

            consumers.draw(VeinMinerRenderLayer.getWireframeTransparent());

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

    private static KeyBinding registerKeybind(String id, int key) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.veinminer4bukkit." + id,
            InputUtil.Type.KEYSYM,
            key,
            "category.veinminer4bukkit.general"
        ));
    }

}
