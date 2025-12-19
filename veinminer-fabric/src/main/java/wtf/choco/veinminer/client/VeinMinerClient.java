package wtf.choco.veinminer.client;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wtf.choco.network.fabric.FabricProtocolConfiguration;
import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.client.network.FabricServerState;
import wtf.choco.veinminer.client.network.VeinMinerFabricChannelRegistrar;
import wtf.choco.veinminer.client.render.WireframeShapeRenderer;
import wtf.choco.veinminer.client.render.layer.PatternWheelHudElement;
import wtf.choco.veinminer.client.render.layer.VeinMiningIconHudElement;
import wtf.choco.veinminer.network.protocol.serverbound.ServerboundHandshake;

/**
 * The Fabric VeinMiner mod entry class.
 */
public final class VeinMinerClient implements ClientModInitializer {

    private static final KeyMapping.Category VEINMINER_KEY_MAPPING_CATEGORY = KeyMapping.Category.register(ResourceLocation.fromNamespaceAndPath("veinminer_companion", "general"));
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

    private static final Logger LOGGER = LoggerFactory.getLogger("veinminer_companion");

    private FabricServerState serverState;

    private final KeyHandler keyHandler = new KeyHandler(this);
    private final BlockLookUpdateHandler blockLookUpdateHandler = new BlockLookUpdateHandler(this);

    private final PatternWheelHudElement patternWheelRenderComponent = new PatternWheelHudElement(this);

    private final WireframeShapeRenderer wireframeShapeRenderer = new WireframeShapeRenderer(this);

    @Override
    public void onInitializeClient() {
        VeinMiner.PROTOCOL.registerChannels(new VeinMinerFabricChannelRegistrar(this, LOGGER));
        VeinMiner.PROTOCOL.configure(new FabricProtocolConfiguration(true));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            this.keyHandler.tick();
            this.blockLookUpdateHandler.tick(client);
            this.patternWheelRenderComponent.tick();
        });

        ClientPlayConnectionEvents.INIT.register((handler, client) -> serverState = new FabricServerState(this, client));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            this.serverState = null;
            this.blockLookUpdateHandler.reset();
        });

        // Once joined, we're going to send a handshake packet to let the server know we have the client mod installed
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> serverState.sendMessage(new ServerboundHandshake(VeinMiner.PROTOCOL.getVersion())));

        HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS, PatternWheelHudElement.ID, patternWheelRenderComponent);
        HudElementRegistry.attachElementAfter(VanillaHudElements.CROSSHAIR, VeinMiningIconHudElement.ID, new VeinMiningIconHudElement(this));

        WorldRenderEvents.END_MAIN.register(wireframeShapeRenderer::render);
    }

    /**
     * Check whether or not the client is connected to a server and has a valid server state.
     * <p>
     * The result of this method should be checked before accessing {@link #getServerState()}.
     *
     * @return true if a server state is available, false otherwise
     */
    public boolean hasServerState() {
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
    public FabricServerState getServerState() {
        if (!hasServerState()) {
            throw new IllegalStateException("Tried to get FabricServerState while not connected to server.");
        }

        return serverState;
    }

    /**
     * Get the {@link BlockLookUpdateHandler} instance.
     *
     * @return the block look update handler
     */
    @NotNull
    public BlockLookUpdateHandler getBlockLookUpdateHandler() {
        return blockLookUpdateHandler;
    }

    /**
     * Get the layer for the pattern wheel.
     *
     * @return the pattern wheel layer
     */
    @NotNull
    public PatternWheelHudElement getPatternWheelLayer() {
        return patternWheelRenderComponent;
    }

    private static KeyMapping registerKeyMapping(String id, int key) {
        return KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.veinminer_companion." + id,
            InputConstants.Type.KEYSYM,
            key,
            VEINMINER_KEY_MAPPING_CATEGORY
        ));
    }

}
