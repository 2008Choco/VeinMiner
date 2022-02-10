package wtf.choco.veinminer.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;

import org.jetbrains.annotations.NotNull;

/**
 * A simple component capable of being rendered on the Minecraft HUD.
 */
public interface HudRenderComponent {

    /**
     * Render the component to the given {@link MatrixStack}.
     *
     * @param client the client instance
     * @param stack the stack to which the hud is being rendered
     * @param tickDelta tick delta time
     */
    public void render(@NotNull MinecraftClient client, @NotNull MatrixStack stack, float tickDelta);

    /**
     * Check whether or not this component should be rendered to the screen.
     *
     * @return true if should be rendered, false otherwise
     */
    public boolean shouldRender();

}
