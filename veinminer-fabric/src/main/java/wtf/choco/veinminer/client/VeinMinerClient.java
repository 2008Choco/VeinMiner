package wtf.choco.veinminer.client;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.KeyMapping;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wtf.choco.network.fabric.FabricProtocolConfiguration;
import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.client.network.FabricServerState;
import wtf.choco.veinminer.client.network.VeinMinerFabricChannelRegistrar;
import wtf.choco.veinminer.client.render.WireframeShapeRenderer;
import wtf.choco.veinminer.client.render.hud.PatternWheelHudComponent;
import wtf.choco.veinminer.client.render.hud.HudComponentRenderer;
import wtf.choco.veinminer.client.render.hud.VeinMiningIconHudComponent;
import wtf.choco.veinminer.network.protocol.serverbound.ServerboundHandshake;

/**
 * The Fabric VeinMiner mod entry class.
 */
public final class VeinMinerClient implements ClientModInitializer {

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
    private final BlockLookUpdateHandler wireframeUpdateHandler = new BlockLookUpdateHandler(this);

    private final PatternWheelHudComponent patternWheelRenderComponent = new PatternWheelHudComponent();
    private final HudComponentRenderer hudComponentRenderer = new HudComponentRenderer(this,
            new VeinMiningIconHudComponent(),
            patternWheelRenderComponent
    );
    private final WireframeShapeRenderer wireframeShapeRenderer = new WireframeShapeRenderer(this);

    @Override
    public void onInitializeClient() {
        VeinMiner.PROTOCOL.registerChannels(new VeinMinerFabricChannelRegistrar(this, LOGGER));
        VeinMiner.PROTOCOL.configure(new FabricProtocolConfiguration(true));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            this.keyHandler.tick();
            this.wireframeUpdateHandler.updateLastLookedPosition(client);
        });

        ClientPlayConnectionEvents.INIT.register((handler, client) -> serverState = new FabricServerState(client));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> serverState = null);

        // Once joined, we're going to send a handshake packet to let the server know we have the client mod installed
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> serverState.sendMessage(new ServerboundHandshake(VeinMiner.PROTOCOL.getVersion())));

        HudRenderCallback.EVENT.register(hudComponentRenderer::render);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(wireframeShapeRenderer::render);
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
     * Get the HudComponent for the pattern wheel.
     *
     * @return the pattern wheel hud component
     */
    @NotNull
    public PatternWheelHudComponent getPatternWheelRenderComponent() {
        return patternWheelRenderComponent;
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
