package wtf.choco.veinminer.anticheat;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.reflect.MethodUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMiner;

/**
 * The default Spartan hook implementation
 */
public final class AntiCheatHookSpartan implements AntiCheatHook, Listener {

    private Method methodGetPlayer;

    private final Set<@NotNull UUID> exempt = new HashSet<>();

    public AntiCheatHookSpartan(@NotNull VeinMiner plugin) {
        Class<? extends Event> eventClass = null;

        try {
            eventClass = ClassUtils.getClass("me.vagdedes.spartan.api.PlayerViolationEvent");
            this.methodGetPlayer = MethodUtils.getAccessibleMethod(eventClass, "getPlayer", new Class<?>[] {});
        } catch (ClassNotFoundException e) {
            this.sendIncompatibleMessage(plugin);
            this.methodGetPlayer = null;
            return;
        }

        //Registers the player violation event reflectively since spartan doesn't have a repo.
        Bukkit.getPluginManager().registerEvent(eventClass, this, EventPriority.LOWEST, (listener, event) -> {
            Player player;

            try {
                player = (Player) methodGetPlayer.invoke(event);
            } catch (ReflectiveOperationException e) {
                return;
            }

            if (!exempt.contains(player.getUniqueId())) {
                return;
            }

            ((Cancellable) event).setCancelled(true);
        }, plugin);
    }

    private void sendIncompatibleMessage(@NotNull VeinMiner veinMiner) {
        veinMiner.getLogger().severe("The version of " + getPluginName() + " on this server is incompatible with VeinMiner. Please post information on the spigot resource discussion page.");
    }

    @NotNull
    @Override
    public String getPluginName() {
        return "Spartan";
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
