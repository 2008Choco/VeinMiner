package wtf.choco.veinminer.anticheat;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMinerPlugin;

/**
 * The default Grim AntiCheat hook implementation.
 */
public final class AntiCheatHookGrim implements AntiCheatHook {

    /*
     * Unfortunately, in Grim 2.3.59 they decided to move the package declaration to "api". This is fine,
     * it's correct. It should have been under an API package to begin with. But this now fricks up the way
     * I write VeinMiner's Grim hook and forces me to use reflection unless I want to modularize VeinMiner's
     * Bukkit code JUST for different Grim implementations... which I really do not want to do :(
     *
     * TODO: Once usage of GrimAC exceeds 75%, use of reflection should be dropped in favour of Grim's API
     * again, thus dropping support for 2.3.58 and below.
     */

    // FlagEvent is called asynchronously, so we need a ConcurrentHashMap to be certain
    private final Set<UUID> exempt = ConcurrentHashMap.newKeySet();

    private Method methodFlagEventGetPlayer;
    private Method methodGrimUserGetUniqueId;

    private boolean supported;

    public AntiCheatHookGrim(@NotNull VeinMinerPlugin plugin) {
        try {
            Class<? extends Event> eventClass = findClass(Event.class, "ac.grim.grimac.events.FlagEvent", "ac.grim.grimac.api.events.FlagEvent");
            Class<?> grimUserClass = findClass("ac.grim.grimac.GrimUser", "ac.grim.grimac.api.GrimUser");

            this.methodFlagEventGetPlayer = eventClass.getMethod("getPlayer");
            this.methodGrimUserGetUniqueId = grimUserClass.getMethod("getUniqueId");
            this.supported = (methodFlagEventGetPlayer != null && methodGrimUserGetUniqueId != null);

            if (supported) {
                Bukkit.getPluginManager().registerEvent(eventClass, new Listener() {}, EventPriority.NORMAL, (listener, event) -> handleFlagEvent((Cancellable) event), plugin);
            }
        } catch (ReflectiveOperationException e) {
            plugin.getLogger().severe("The version of GrimAC on this server is incompatible with Veinminer. Please post information on the spigot resource discussion page.");
            this.supported = false;
        }
    }

    private <T> Class<? extends T> findClass(Class<T> subclass, String... classNames) throws ClassNotFoundException {
        for (String string : classNames) {
            try {
                Class<? extends T> clazz = Class.forName(string).asSubclass(subclass);
                return clazz;
            } catch (ReflectiveOperationException ignore) { }
        }

        throw new ClassNotFoundException();
    }

    private Class<?> findClass(String... classNames) throws ClassNotFoundException {
        return findClass(Object.class, classNames);
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

    private void handleFlagEvent(Cancellable event) {
        if (event.isCancelled()) {
            return;
        }

        try {
            Object player = methodFlagEventGetPlayer.invoke(event);
            UUID playerUUID = (UUID) methodGrimUserGetUniqueId.invoke(player);
            if (!exempt.contains(playerUUID)) {
                return;
            }
        } catch (ReflectiveOperationException e) {
            return;
        }

        event.setCancelled(true);
    }

}
