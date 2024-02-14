package wtf.choco.veinminer.client.render.hud;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.client.network.FabricServerState;
import wtf.choco.veinminer.config.ClientConfig;

/**
 * A simple component capable of being rendered on the Minecraft HUD.
 *
 * @see HudComponentRenderer
 */
public interface HudComponent {

    /**
     * Render the component to the given {@link PoseStack}.
     *
     * @param client the client instance
     * @param serverState the current server state
     * @param graphics the gui graphics
     * @param tickDelta tick delta time
     */
    public void render(@NotNull Minecraft client, @NotNull FabricServerState serverState, @NotNull GuiGraphics graphics, float tickDelta);

    /**
     * Check whether or not this component should be rendered to the screen.
     *
     * @param config the client configuration
     * @param serverState the current server state
     *
     * @return true if should be rendered, false otherwise
     */
    public boolean shouldRender(@NotNull ClientConfig config, @NotNull FabricServerState serverState);

    /**
     * Tick this component.
     */
    public default void tick() {}

}
