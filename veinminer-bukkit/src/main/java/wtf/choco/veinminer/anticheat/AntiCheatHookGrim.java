package wtf.choco.veinminer.anticheat;

import ac.grim.grimac.api.AbstractCheck;
import ac.grim.grimac.api.GrimAPIProvider;
import ac.grim.grimac.api.GrimUser;
import ac.grim.grimac.api.event.events.FlagEvent;
import ac.grim.grimac.api.plugin.GrimPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import wtf.choco.veinminer.VeinMinerPlugin;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * The default Grim AntiCheat hook implementation.
 */
public final class AntiCheatHookGrim implements AntiCheatHook, Listener {

    // FlagEvent is called asynchronously, so we need a ConcurrentHashMap to be certain
    private final Set<UUID> exempt = ConcurrentHashMap.newKeySet();

    private boolean supported;

    public AntiCheatHookGrim(@NotNull VeinMinerPlugin plugin) {
        try {
            GrimPlugin grimPlugin = GrimAPIProvider.get().getGrimPlugin(plugin);
            GrimAPIProvider.get().getEventBus().get(FlagEvent.class).onFlagSupplier(grimPlugin, this::onFlag);
            this.supported = true;
        } catch (Throwable e) {
            this.supported = false;
        }
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

    @Override
    public boolean isSupported() {
        return supported;
    }

    private boolean onFlag(GrimUser user, AbstractCheck check, Supplier<String> verbose, boolean currentlyCancelled) {
        if (exempt.contains(user.getUniqueId())) {
            return true; // Don't process Grim flag while exempt
        }

        return currentlyCancelled;
    }

}
