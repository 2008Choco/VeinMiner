package me.choco.veinminer.events;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import me.konsolas.aac.api.PlayerViolationEvent;

public class AntiCheatSupport implements Listener {
	
	private final List<UUID> exemptedUsers = new ArrayList<>();
	
	// Prevent Advanced Anti-Cheat to flag VeinMiner users
	@EventHandler(priority=EventPriority.LOWEST)
	public void onAACViolation(PlayerViolationEvent event) {
		if (!exemptedUsers.contains(event.getPlayer().getUniqueId())) return;
		event.setCancelled(true);
	}
	
	/** 
	 * Exempt a user from Advanced Anti Cheat violation
	 * 
	 * @param player the player to exempt
	 */
	public void exemptFromViolation(Player player) {
		Preconditions.checkArgument(player != null, "Cannot exempt null player from violation checks");
		this.exemptedUsers.add(player.getUniqueId());
	}
	
	/** 
	 * Check whether a user is exempted from Advanced Anti Cheat violation or not
	 * 
	 * @param player the player to check
	 * @return true if the user is exempted
	 */
	public boolean isExempted(Player player) {
		Preconditions.checkArgument(player != null, "Cannot check for exemption of a null player");
		return this.exemptedUsers.contains(player.getUniqueId());
	}
	
	/** 
	 * Unexempt a user from Advanced Anti Cheat violation
	 * 
	 * @param player the player to unexempt
	 */
	public void unexemptFromViolation(Player player) {
		Preconditions.checkArgument(player != null, "Cannot unexempt null player from violation checks");
		this.exemptedUsers.remove(player.getUniqueId());
	}
	
	/**
	 * Clear all exempted anti cheat users
	 */
	public void clearExemptedUsers() {
		this.exemptedUsers.clear();
	}
	
	/** 
	 * Get a list of exempted users
	 * 
	 * @return a list of exempted users
	 */
	public List<UUID> getExemptedUsers() {
		return ImmutableList.copyOf(exemptedUsers);
	}
}