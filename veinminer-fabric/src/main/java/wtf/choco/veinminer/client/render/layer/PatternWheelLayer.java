package wtf.choco.veinminer.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.client.VeinMinerClient;
import wtf.choco.veinminer.client.network.FabricServerState;

/**
 * A {@link VeinMinerIdentifiedLayer} for the pattern selection wheel in the top left.
 */
public final class PatternWheelLayer extends VeinMinerIdentifiedLayer {

    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("veinminer_companion", "pattern_wheel");

    private static final int STAY_TICKS = 60; // 3 seconds
    private static final int FADE_TICKS = 4; // 0.2 seconds
    private static final int TOTAL_TICKS = STAY_TICKS + (FADE_TICKS * 2);

    private int remainingTicks = 0;

    public PatternWheelLayer(VeinMinerClient client) {
        super(client);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void render(@NotNull FabricServerState serverState, @NotNull GuiGraphics graphics, @NotNull DeltaTracker delta) {
        Profiler.get().push("veinminerPatternWheel");

        float actualRemainingMs = remainingTicks - delta.getGameTimeDeltaTicks();

        // Calculate alpha based on progress of fade in/out times
        float alpha = 255.0F;
        if (remainingTicks >= (STAY_TICKS + FADE_TICKS)) { // Fade in
            float elapsed = TOTAL_TICKS - actualRemainingMs;
            alpha = Math.min((int) (elapsed * 255.0F / FADE_TICKS), 255);
        }
        else if (remainingTicks < FADE_TICKS) { // Fade out
            alpha = Math.min((int) (actualRemainingMs * 255.0F / FADE_TICKS), 255);
        }

        // Final colour with alpha included
        int finalAlpha = (Mth.floor(alpha) << 24) & 0xFF000000;
        int colour = 0xFFFFFF | finalAlpha;

        String before = serverState.getPreviousPattern().toString();
        String selected = serverState.getSelectedPattern().toString();
        String after = serverState.getNextPattern().toString();

        Minecraft minecraft = Minecraft.getInstance();
        PoseStack stack = graphics.pose();
        stack.pushPose();
        stack.scale(1.1F, 1.1F, 1.1F);
        graphics.drawString(minecraft.font, Component.literal(selected), 4, minecraft.font.lineHeight, colour);

        stack.scale(0.6F, 0.6F, 0.6F);
        graphics.drawString(minecraft.font, Component.literal(before), 7, 4, colour);
        graphics.drawString(minecraft.font, Component.literal(after), 7, 5 + (minecraft.font.lineHeight * 3), colour);
        stack.popPose();

        Profiler.get().pop();
    }

    @Override
    public boolean shouldRender(@NotNull FabricServerState serverState) {
        return serverState.getConfig().isAllowPatternSwitchingKeybind() && serverState.hasPatternKeys() && remainingTicks > 0;
    }

    public void tick() {
        if (remainingTicks > 0) {
            this.remainingTicks--;
        }
    }

    /**
     * Push a render cycle to the component.
     * <p>
     * If this wheel is not being rendered, it will start rendering. If it is fading in, it will
     * continue to fade in. If it is during its stay period, it will reset to the start of the stay
     * period. If it is fading out, it will start fading in starting from the current fade out time.
     */
    public void pushRender() {
        if (remainingTicks <= 0) { // If not rendered, set to max time
            this.remainingTicks = TOTAL_TICKS;
        }
        else if (remainingTicks < FADE_TICKS) { // If fading out, fade back in starting at current fade out point
            this.remainingTicks = TOTAL_TICKS - remainingTicks;
        }
        else if (remainingTicks < (TOTAL_TICKS - FADE_TICKS)) { // If already faded in (during stay time), skip fade in time
            this.remainingTicks = TOTAL_TICKS - FADE_TICKS;
        }
    }

}
