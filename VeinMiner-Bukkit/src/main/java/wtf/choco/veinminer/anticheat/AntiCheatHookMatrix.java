package wtf.choco.veinminer.anticheat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import me.rerere.matrix.api.MatrixAPIProvider;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

/**
 * The default Matrix hook implementation
 */
public final class AntiCheatHookMatrix implements AntiCheatHook, Listener {

    private final Set<UUID> bypassing = new HashSet<>();

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

        MatrixAPIProvider.getAPI().setBypass(player, true);
        this.bypassing.add(player.getUniqueId());
    }

    @Override
    public void unexempt(@NotNull Player player) {
        MatrixAPIProvider.getAPI().setBypass(player, false);
        this.bypassing.remove(player.getUniqueId());
    }

    @Override
    public boolean shouldUnexempt(@NotNull Player player) {
        return bypassing.contains(player.getUniqueId());
    }

}
