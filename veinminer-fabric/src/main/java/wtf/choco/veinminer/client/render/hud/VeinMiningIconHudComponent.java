package wtf.choco.veinminer.client.render.hud;

import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.client.network.FabricServerState;
import wtf.choco.veinminer.config.ClientConfig;

/**
 * A {@link HudComponent} for the vein mining icon at the user's crosshair.
 */
public final class VeinMiningIconHudComponent implements HudComponent {

    private static final ResourceLocation VEINMINER_ICON_LOCATION = ResourceLocation.fromNamespaceAndPath("veinminer_companion", "textures/gui/veinminer_icon.png");

    @Override
    public void render(@NotNull Minecraft client, @NotNull FabricServerState serverState, @NotNull GuiGraphics graphics, float delta) {
        client.getProfiler().push("veinMiningIcon");

        Window window = client.getWindow();
        int width = window.getGuiScaledWidth(), height = window.getGuiScaledHeight();

        graphics.blit(VEINMINER_ICON_LOCATION, (width / 2) + 8, (height / 2) - 4, 0, 0, 8, 8, 8, 8);

        client.getProfiler().pop();
    }

    @Override
    public boolean shouldRender(@NotNull ClientConfig config, @NotNull FabricServerState serverState) {
        return config.isAllowActivationKeybind() && Minecraft.renderNames() && serverState.isActive();
    }

}
