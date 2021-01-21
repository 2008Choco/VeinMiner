package wtf.choco.veinminer.anticheat;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.reflect.MethodUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wtf.choco.veinminer.VeinMiner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * The default Spartan hook implementation
 */
public final class AntiCheatHookSpartan implements AntiCheatHook, Listener {
    private final @Nullable Method getPlayer;

    private final Set<@NotNull UUID> exempt = new HashSet<>();

    @SuppressWarnings("unchecked")
    public AntiCheatHookSpartan(@NotNull VeinMiner veinMiner) {
        //It is assumed spartan is on the server if the "Spartan" plugin is enabled.

        Class<? extends Event> eventClass = null;
        try {
            eventClass = ClassUtils.getClass("me.vagdedes.spartan.api.PlayerViolationEvent");
        } catch (ClassNotFoundException e) {
            this.sendIncompatibleMessage(veinMiner);
            this.getPlayer = null;
            return;
        }
        this.getPlayer = MethodUtils.getAccessibleMethod(eventClass, "getPlayer", new Class<?>[] {});

        //Registers the player violation event reflectively since spartan doesn't have a repo.
        Bukkit.getPluginManager().registerEvent(
                eventClass,
                this,
                EventPriority.LOWEST,
                (listener, event) -> {
                    Player player;
                    try {
                        player = (Player) this.getPlayer.invoke(event);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        this.sendIncompatibleMessage(veinMiner);
                        //unregister the listener since it's not compatible with Veinminer.
                        event.getHandlers().unregister(this);
                        return;
                    }

                    //Check if player is exempt.
                    if (!this.exempt.contains(player.getUniqueId())) {
                        return;
                    }

                    //Cancel the event if they are exempt.
                    ((Cancellable) event).setCancelled(true);
                },
                veinMiner
        );
    }

    private void sendIncompatibleMessage(@NotNull VeinMiner veinMiner) {
        veinMiner.getLogger().severe(() ->
                String.format(
                        "The version of %s on this server is incompatible with Veinminer."
                                + "Please post information on the spigot resource discussion page.",
                        this.getPluginName()
                )
        );
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
