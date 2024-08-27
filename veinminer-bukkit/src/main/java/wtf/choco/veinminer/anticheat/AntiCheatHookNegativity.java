package wtf.choco.veinminer.anticheat;

import com.elikill58.negativity.universal.bypass.BypassManager;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class AntiCheatHookNegativity implements AntiCheatHook {

    private final Set<UUID> exempt = new HashSet<>();

    public AntiCheatHookNegativity() {
        BypassManager.addBypassChecker((player, cheat) -> exempt.contains(player.getUniqueId()));
    }

    @Override
    public void exempt(@NotNull Player player) {
        this.exempt.add(player.getUniqueId());
    }

    @Override
    public void unexempt(@NotNull Player player) {
        this.exempt.remove(player.getUniqueId());
    }

    @Override
    public boolean shouldUnexempt(@NotNull Player player) {
        return exempt.contains(player.getUniqueId());
    }

}
