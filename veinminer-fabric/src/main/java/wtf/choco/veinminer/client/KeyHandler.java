package wtf.choco.veinminer.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.client.network.FabricServerState;
import wtf.choco.veinminer.client.render.layer.PatternWheelLayer;
import wtf.choco.veinminer.config.ClientConfig;
import wtf.choco.veinminer.network.protocol.serverbound.ServerboundRequestVeinMine;
import wtf.choco.veinminer.network.protocol.serverbound.ServerboundToggleVeinMiner;

/**
 * A class handling key press logic.
 */
public final class KeyHandler {

    private boolean changingPatterns = false;

    private final VeinMinerClient client;

    KeyHandler(@NotNull VeinMinerClient client) {
        this.client = client;
    }

    /**
     * Tick the key logic.
     */
    public void tick() {
        if (!client.hasServerState()) {
            return;
        }

        FabricServerState serverState = client.getServerState();
        if (!serverState.isEnabledOnServer()) {
            return;
        }

        ClientConfig config = serverState.getConfig();

        this.handleActivationKeybind(serverState, config);
        this.handlePatternSwitchKeybinds(serverState, config);
    }

    private void handleActivationKeybind(@NotNull FabricServerState serverState, @NotNull ClientConfig config) {
        if (!config.isAllowActivationKeybind()) {
            return;
        }

        boolean lastActive = serverState.isActive();
        boolean active = VeinMinerClient.KEY_MAPPING_ACTIVATE_VEINMINER.isDown();
        if (lastActive == active) {
            return;
        }

        serverState.setActive(active);
        serverState.sendMessage(new ServerboundToggleVeinMiner(active));

        // If the player is activating vein miner and looking at a block, we also need to update the voxel shape
        if (active) {
            Minecraft minecraft = Minecraft.getInstance();
            if (!(minecraft.hitResult instanceof BlockHitResult hit)) {
                return;
            }

            BlockPos position = hit.getBlockPos();
            serverState.resetShape();
            serverState.sendMessage(new ServerboundRequestVeinMine(position.getX(), position.getY(), position.getZ()));
        }
    }

    private void handlePatternSwitchKeybinds(@NotNull FabricServerState serverState, @NotNull ClientConfig config) {
        if (!config.isAllowPatternSwitchingKeybind()) {
            return;
        }

        boolean lastChangingPatterns = changingPatterns;
        this.changingPatterns = (VeinMinerClient.KEY_MAPPING_NEXT_PATTERN.isDown() || VeinMinerClient.KEY_MAPPING_PREVIOUS_PATTERN.isDown());

        if (lastChangingPatterns ^ changingPatterns) {
            boolean next;

            // There has to be a smarter way to write this...
            if (VeinMinerClient.KEY_MAPPING_NEXT_PATTERN.isDown()) {
                next = true;
            } else if (VeinMinerClient.KEY_MAPPING_PREVIOUS_PATTERN.isDown()) {
                next = false;
            } else {
                return;
            }

            PatternWheelLayer patternWheel = client.getPatternWheelLayer();

            // If the HUD wheel isn't rendered yet, push a render call but don't change the pattern
            if (patternWheel.shouldRender(serverState)) {
                serverState.changePattern(next);
            }

            patternWheel.pushRender();
        }
    }

}
