package wtf.choco.veinminer.client.render.hud;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.config.ClientConfig;

/**
 * A simple component capable of being rendered on the Minecraft HUD.
 */
public interface HudRenderComponent {

    /**
     * Render the component to the given {@link PoseStack}.
     *
     * @param client the client instance
     * @param graphics the gui graphics
     * @param tickDelta tick delta time
     */
    public void render(@NotNull Minecraft client, @NotNull GuiGraphics graphics, float tickDelta);

    /**
     * Check whether or not this component should be rendered to the screen.
     *
     * @param config the client configuration
     *
     * @return true if should be rendered, false otherwise
     */
    public boolean shouldRender(@NotNull ClientConfig config);

}
