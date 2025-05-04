package wtf.choco.veinminer.client.render.layer;

import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.Profiler;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.client.VeinMinerClient;
import wtf.choco.veinminer.client.network.FabricServerState;

/**
 * A {@link VeinMinerIdentifiedLayer} for the vein mining icon at the user's crosshair.
 */
public final class VeinMiningIconLayer extends VeinMinerIdentifiedLayer {

    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("veinminer_companion", "vein_mining_icon");
    private static final ResourceLocation VEINMINER_ICON_LOCATION = ResourceLocation.fromNamespaceAndPath("veinminer_companion", "textures/gui/veinminer_icon.png");

    public VeinMiningIconLayer(VeinMinerClient client) {
        super(client);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void render(@NotNull FabricServerState serverState, @NotNull GuiGraphics graphics, @NotNull DeltaTracker delta) {
        Profiler.get().push("veinMiningIcon");

        Window window = Minecraft.getInstance().getWindow();
        int width = window.getGuiScaledWidth(), height = window.getGuiScaledHeight();

        graphics.blit(RenderType::guiTextured, VEINMINER_ICON_LOCATION, (width / 2) + 8, (height / 2) - 4, 0, 0, 8, 8, 8, 8);

        Profiler.get().pop();
    }

    @Override
    public boolean shouldRender(@NotNull FabricServerState serverState) {
        return serverState.getConfig().isAllowActivationKeybind() && Minecraft.renderNames() && serverState.isActive();
    }

}
