package wtf.choco.veinminer.client.render.layer;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.client.VeinMinerClient;
import wtf.choco.veinminer.client.network.FabricServerState;

public abstract class VeinMinerHudElement implements HudElement {

    private final VeinMinerClient client;

    public VeinMinerHudElement(VeinMinerClient client) {
        this.client = client;
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker delta) {
        if (shouldRender()) {
            this.render(client.getServerState(), graphics, delta);
        }
    }

    public abstract void render(@NotNull FabricServerState state, @NotNull GuiGraphics graphics, @NotNull DeltaTracker delta);

    private boolean shouldRender() {
        if (!client.hasServerState()) {
            return false;
        }

        FabricServerState state = client.getServerState();
        return state.isEnabledOnServer() && shouldRender(state);
    }

    public abstract boolean shouldRender(@NotNull FabricServerState state);

}
