package wtf.choco.veinminer.hud;

import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMinerMod;
import wtf.choco.veinminer.config.ClientConfig;

/**
 * A {@link HudRenderComponent} for the vein mining icon at the user's crosshair.
 */
public final class HudRenderComponentVeinMiningIcon implements HudRenderComponent {

    private static final ResourceLocation VEINMINER_ICON_LOCATION = new ResourceLocation("veinminer_companion", "textures/gui/veinminer_icon.png");

    @Override
    public void render(@NotNull Minecraft client, @NotNull GuiGraphics graphics, float delta) {
        client.getProfiler().push("veinMiningIcon");

        Window window = client.getWindow();
        int width = window.getGuiScaledWidth(), height = window.getGuiScaledHeight();

        graphics.blit(VEINMINER_ICON_LOCATION, (width / 2) + 8, (height / 2) - 4, 0, 0, 8, 8, 8, 8);

        client.getProfiler().pop();
    }

    @Override
    public boolean shouldRender(@NotNull ClientConfig config) {
        return config.isAllowActivationKeybind() && Minecraft.renderNames() && VeinMinerMod.getServerState().isActive();
    }

}
