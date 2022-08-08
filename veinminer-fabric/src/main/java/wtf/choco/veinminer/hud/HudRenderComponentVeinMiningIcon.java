package wtf.choco.veinminer.hud;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMinerMod;
import wtf.choco.veinminer.config.ClientConfig;

/**
 * A {@link HudRenderComponent} for the vein mining icon at the user's crosshair.
 */
public final class HudRenderComponentVeinMiningIcon implements HudRenderComponent {

    private static final ResourceLocation TEXTURE_VEINMINER_ICON = new ResourceLocation("veinminer_companion", "textures/gui/veinminer_icon.png");

    @Override
    public void render(@NotNull Minecraft client, @NotNull PoseStack stack, float delta) {
        client.getProfiler().push("veinMiningIcon");

        Window window = client.getWindow();
        int width = window.getGuiScaledWidth(), height = window.getGuiScaledHeight();

        stack.pushPose();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE_VEINMINER_ICON);
        GuiComponent.blit(stack, (width / 2) + 8, (height / 2) - 4, 0, 0, 8, 8, 8, 8);

        stack.popPose();

        client.getProfiler().pop();
    }

    @Override
    public boolean shouldRender(@NotNull ClientConfig config) {
        return config.isAllowActivationKeybind() && Minecraft.renderNames() && VeinMinerMod.getServerState().isActive();
    }

}
