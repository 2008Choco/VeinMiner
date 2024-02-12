package wtf.choco.veinminer.api.event.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.player.ActivationStrategy;

/**
 * Called when a player toggles the vein mine state using a client-sided mod.
 * <p>
 * Note that this event is only fired for players that have installed the client-sided
 * VeinMiner mod when {@link ActivationStrategy#CLIENT} is enabled and the client-configured
 * button was pressed or released.
 * <p>
 * The key that was pressed is not included in this event, only the action that the key was
 * pressed and has changed the player's activation state.
 */
public class PlayerClientActivateVeinMinerEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancelled = false;

    private final boolean activated;

    /**
     * Construct a new {@link PlayerClientActivateVeinMinerEvent}.
     *
     * @param player the player that changed activation state
     * @param activated the new activation state
     */
    public PlayerClientActivateVeinMinerEvent(@NotNull Player player, boolean activated) {
        super(player);
        this.activated = activated;
    }

    /**
     * Get the new activation state from the client. If true, the client pressed the
     * activation key. If false, the key was released.
     *
     * @return true if activated, false otherwise
     */
    public boolean isActivated() {
        return activated;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
