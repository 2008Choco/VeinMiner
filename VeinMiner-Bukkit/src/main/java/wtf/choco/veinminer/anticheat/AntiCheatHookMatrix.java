package wtf.choco.veinminer.anticheat;

import me.rerere.matrix.api.HackType;
import me.rerere.matrix.api.MatrixAPIProvider;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

/**
 * The default Matrix hook implementation
 */
public final class AntiCheatHookMatrix implements AntiCheatHook, Listener {

    @NotNull
    @Override
    public String getPluginName() {
        return "Matrix";
    }

    @Override
    public void exempt(@NotNull Player player) {
        if (MatrixAPIProvider.getAPI().isBypass(player)) {
            return;
        }

        MatrixAPIProvider.getAPI().tempBypass(player, HackType.BLOCK, 100L);
    }

    @Override
    public void unexempt(@NotNull Player player) {
    }

    @Override
    public boolean shouldUnexempt(@NotNull Player player) {
        return false;
    }
}
