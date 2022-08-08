package wtf.choco.veinminer;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;

import java.util.Objects;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.config.ClientConfig;
import wtf.choco.veinminer.hud.HudRenderComponent;
import wtf.choco.veinminer.hud.HudRenderComponentPatternWheel;
import wtf.choco.veinminer.hud.HudRenderComponentVeinMiningIcon;
import wtf.choco.veinminer.network.FabricChannelRegistrar;
import wtf.choco.veinminer.network.FabricServerState;
import wtf.choco.veinminer.network.protocol.serverbound.PluginMessageServerboundHandshake;
import wtf.choco.veinminer.network.protocol.serverbound.PluginMessageServerboundRequestVeinMine;
import wtf.choco.veinminer.network.protocol.serverbound.PluginMessageServerboundToggleVeinMiner;
import wtf.choco.veinminer.render.VeinMinerRenderType;

/**
 * The Fabric VeinMiner mod entry class.
 */
public final class VeinMinerMod implements ClientModInitializer {

    /**
     * The "activate veinminer" key mapping. Defaults to ~
     */
    public static final KeyMapping KEY_MAPPING_ACTIVATE_VEINMINER = registerKeyMapping("activate_veinminer", InputConstants.KEY_GRAVE);
    /**
     * The "next pattern" key mapping. Defaults to ]
     */
    public static final KeyMapping KEY_MAPPING_NEXT_PATTERN = registerKeyMapping("next_pattern", InputConstants.KEY_RBRACKET);
    /**
     * The "previous pattern" key mapping. Defaults to [
     */
    public static final KeyMapping KEY_MAPPING_PREVIOUS_PATTERN = registerKeyMapping("previous_pattern", InputConstants.KEY_LBRACKET);

    private static FabricServerState serverState;

    private final HudRenderComponentPatternWheel patternWheelRenderComponent = new HudRenderComponentPatternWheel();
    private final HudRenderComponent[] hudRenderComponents = {
            new HudRenderComponentVeinMiningIcon(),
            patternWheelRenderComponent
    };

    private boolean changingPatterns = false;

    @SuppressWarnings("removal") // VeinMiner.PROTOCOL_LEGACY
    @Override
    public void onInitializeClient() {
        FabricChannelRegistrar channelRegistrar = new FabricChannelRegistrar();
        VeinMiner.PROTOCOL.registerChannels(channelRegistrar);
        VeinMiner.PROTOCOL_LEGACY.registerChannels(channelRegistrar);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!hasServerState() || !getServerState().isEnabledOnServer()) {
                return;
            }

            ClientConfig config = getServerState().getConfig();

            boolean shouldRequestVeinMine = false;
            boolean active = KEY_MAPPING_ACTIVATE_VEINMINER.isDown();

            if (config.isAllowActivationKeybind()) {
                boolean lastActive = getServerState().isActive();
                getServerState().setActive(active = KEY_MAPPING_ACTIVATE_VEINMINER.isDown());

                // Activating / deactivating vein miner
                if (lastActive ^ active) {
                    PluginMessageServerboundToggleVeinMiner message = new PluginMessageServerboundToggleVeinMiner(active);

                    VeinMiner.PROTOCOL.sendMessageToServer(serverState, message);
                    VeinMiner.PROTOCOL_LEGACY.sendMessageToServer(serverState, message); // LEGACY

                    shouldRequestVeinMine = active;
                }
            }

            HitResult result = client.hitResult;

            if (result instanceof BlockHitResult blockResult) {
                BlockPos position = blockResult.getBlockPos();
                Direction blockFace = blockResult.getDirection();

                // Requesting that the server vein mine at the player's current target block because it's different
                shouldRequestVeinMine |= (active && config.isAllowActivationKeybind() && (!Objects.equals(getServerState().getLastLookedAtBlockPos(), position) || !Objects.equals(getServerState().getLastLookedAtBlockFace(), blockFace)));

                if (shouldRequestVeinMine) {
                    getServerState().resetShape();
                    VeinMiner.PROTOCOL.sendMessageToServer(serverState, new PluginMessageServerboundRequestVeinMine(position.getX(), position.getY(), position.getZ()));
                }

                // Updating the new last looked at position
                if (client.player != null && client.player.level != null && !client.player.level.isEmptyBlock(position)) {
                    getServerState().setLastLookedAt(position, blockFace);
                } else {
                    getServerState().setLastLookedAt(null, null);
                }
            }

            // Changing patterns
            if (config.isAllowPatternSwitchingKeybind()) {
                boolean lastChangingPatterns = changingPatterns;
                this.changingPatterns = (KEY_MAPPING_NEXT_PATTERN.isDown() || KEY_MAPPING_PREVIOUS_PATTERN.isDown());

                if (lastChangingPatterns ^ changingPatterns) {
                    boolean next;

                    // There has to be a smarter way to write this...
                    if (KEY_MAPPING_NEXT_PATTERN.isDown()) {
                        next = true;
                    }
                    else if (KEY_MAPPING_PREVIOUS_PATTERN.isDown()) {
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
            VeinMiner.PROTOCOL_LEGACY.sendMessageToServer(serverState, new PluginMessageServerboundHandshake(VeinMiner.PROTOCOL_LEGACY.getVersion())); // LEGACY
        });

        HudRenderCallback.EVENT.register((stack, tickDelta) -> {
            if (!hasServerState() || !getServerState().isEnabledOnServer()) {
                return;
            }

            Minecraft client = Minecraft.getInstance();
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

            Minecraft client = Minecraft.getInstance();

            // Calculate the stack
            PoseStack stack = context.matrixStack();
            Vec3 position = client.getEntityRenderDispatcher().camera.getPosition();

            stack.pushPose();
            stack.translate(origin.getX() - position.x, origin.getY() - position.y, origin.getZ() - position.z);

            Matrix4f matrix = stack.last().pose();

            // Buffer vertices
            BufferSource source = client.renderBuffers().bufferSource();

            // Full wireframe drawing
            VertexConsumer consumer = source.getBuffer(VeinMinerRenderType.getWireframe());

            shape.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
                consumer.vertex(matrix, (float) x1, (float) y1, (float) z1).color(255, 255, 255, 255).endVertex();
                consumer.vertex(matrix, (float) x2, (float) y2, (float) z2).color(255, 255, 255, 255).endVertex();
            });

            source.endBatch(VeinMinerRenderType.getWireframe());

            // Transparent drawing
            VertexConsumer bufferTransparent = source.getBuffer(VeinMinerRenderType.getWireframeTransparent());

            shape.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
                bufferTransparent.vertex(matrix, (float) x1, (float) y1, (float) z1).color(255, 255, 255, 20).endVertex();
                bufferTransparent.vertex(matrix, (float) x2, (float) y2, (float) z2).color(255, 255, 255, 20).endVertex();
            });

            source.endBatch(VeinMinerRenderType.getWireframeTransparent());

            stack.popPose();
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

    private static KeyMapping registerKeyMapping(String id, int key) {
        return KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.veinminer_companion." + id,
            InputConstants.Type.KEYSYM,
            key,
            "category.veinminer_companion.general"
        ));
    }

}
