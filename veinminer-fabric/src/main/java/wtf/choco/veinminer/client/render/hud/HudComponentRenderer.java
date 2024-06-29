package wtf.choco.veinminer.client.render.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.client.VeinMinerClient;
import wtf.choco.veinminer.client.network.FabricServerState;
import wtf.choco.veinminer.config.ClientConfig;

/**
 * A renderer for {@link HudComponent HudComponents}.
 */
public final class HudComponentRenderer {

    private final VeinMinerClient client;
    private final HudComponent[] hudComponents;

    /**
     * Construct a new {@link HudComponentRenderer}.
     *
     * @param client the client instance
     * @param hudComponents the hud components to render
     */
    public HudComponentRenderer(@NotNull VeinMinerClient client, @NotNull HudComponent @NotNull... hudComponents) {
        this.client = client;
        this.hudComponents = hudComponents;
    }

    /**
     * Render all hud components.
     *
     * @param graphics the GUI graphics instance
     * @param delta the delta time tracker
     */
    public void render(@NotNull GuiGraphics graphics, @NotNull DeltaTracker delta) {
        if (!client.hasServerState()) {
            return;
        }

        FabricServerState serverState = client.getServerState();
        if (!serverState.isEnabledOnServer()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        ClientConfig config = serverState.getConfig();

        for (HudComponent component : hudComponents) {
            if (!component.shouldRender(config, serverState)) {
                continue;
            }

            component.render(client, serverState, graphics, delta.getGameTimeDeltaTicks());
        }
    }

    /**
     * Tick all hud components.
     */
    public void tick() {
        for (HudComponent component : hudComponents) {
            component.tick();
        }
    }

}
