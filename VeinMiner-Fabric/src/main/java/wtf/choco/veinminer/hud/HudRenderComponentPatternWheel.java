package wtf.choco.veinminer.hud;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMinerMod;
import wtf.choco.veinminer.network.FabricServerState;

/**
 * A {@link HudRenderComponent} for the pattern selection wheel in the top left.
 */
public final class HudRenderComponentPatternWheel implements HudRenderComponent {

    private static final float STAY_TIME_MS = 3000F;
    private static final float FADE_MS = 200F;
    private static final float TOTAL_TIME_MS = STAY_TIME_MS + (FADE_MS * 2);

    private float remainingMs = -1L;

    @Override
    public void render(@NotNull MinecraftClient client, @NotNull MatrixStack stack, float tickDelta) {
        client.getProfiler().push("veinminerPatternWheel");

        stack.push();
        stack.translate(4, 0, 0);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Calculate alpha based on progress of fade in/out times
        float alphaProgress = 1.0F;
        if (remainingMs >= (STAY_TIME_MS + FADE_MS)) { // Fade in
            alphaProgress = 1.0F - (remainingMs - (STAY_TIME_MS + FADE_MS)) / FADE_MS;
        }
        else if (remainingMs < FADE_MS) { // Fade out
            alphaProgress = (remainingMs / FADE_MS);
        }

        alphaProgress = MathHelper.clamp(alphaProgress, 0.0F, 1.0F);

        // Final colour with alpha included
        int alpha = (MathHelper.floor(alphaProgress * 255) << 24) & 0xFF000000;
        int colour = 0xFFFFFF | alpha;

        FabricServerState serverState = VeinMinerMod.getServerState();
        String before = serverState.getPreviousPattern().toString();
        String selected = serverState.getSelectedPattern().toString();
        String after = serverState.getNextPattern().toString();

        client.textRenderer.drawWithShadow(stack, Text.of(selected), 0, client.textRenderer.fontHeight, colour);

        stack.push();
        stack.translate(0, 3, 0);
        stack.scale(0.6F, 0.6F, 0.6F);
        client.textRenderer.drawWithShadow(stack, Text.of(before), 0, 0, colour);
        client.textRenderer.drawWithShadow(stack, Text.of(after), 0, client.textRenderer.fontHeight * 3, colour);
        stack.pop();

        RenderSystem.disableBlend();

        stack.pop();

        this.remainingMs -= (client.getLastFrameDuration() * 50);

        client.getProfiler().pop();
    }

    @Override
    public boolean shouldRender() {
        return VeinMinerMod.getServerState().hasPatternKeys() && remainingMs >= 0L;
    }

    /**
     * Push a render cycle to the component.
     * <p>
     * If this wheel is not being rendered, it will start rendering. If it is fading it, it will
     * continue to fade in. If it is during its stay period, it will reset to the start of the stay
     * period. If it is fading out, it will start fading in starting from the current fade out time.
     */
    public void pushRender() {
        System.out.println("-------------");
        if (remainingMs <= 0) { // If not rendered, set to max time
            this.remainingMs = TOTAL_TIME_MS;
        }
        else if (remainingMs < FADE_MS) { // If fading out, fade back in starting at current fade out point
            this.remainingMs = TOTAL_TIME_MS - remainingMs;
        }
        else if (remainingMs < (TOTAL_TIME_MS - FADE_MS)) { // If already faded in (during stay time), skip fade in time
            this.remainingMs = TOTAL_TIME_MS - FADE_MS;
        }
    }

}
