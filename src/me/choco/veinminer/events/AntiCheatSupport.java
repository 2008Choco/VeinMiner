package me.choco.veinminer.events;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import me.konsolas.aac.api.PlayerViolationEvent;

public class AntiCheatSupport implements Listener {
	
	private final List<Player> exemptedUsers = new ArrayList<>();
	
	// Prevent Advanced Anti-Cheat to flag VeinMiner users
	@EventHandler(priority=EventPriority.LOWEST)
	public void onAACViolation(PlayerViolationEvent event){
		if (!exemptedUsers.contains(event.getPlayer())) return;
		event.setCancelled(true);
	}
	
	/** Exempt a user from Anti Cheat violation
	 * @param player - The player to exempt
	 */
	public void exemptFromViolation(Player player){
		this.exemptedUsers.add(player);
	}
	
	/** Check whether a user is exempted from Anti Cheat violation or not
	 * @param player - The player to check
	 * @return true if the user is exempted
	 */
	public boolean isExempted(Player player){
		return this.exemptedUsers.contains(player);
	}
	
	/** Unexempt a user from Anti Cheat violation
	 * @param player - The player to unexempt
	 */
	public void unexemptFromViolation(Player player){
		this.exemptedUsers.remove(player);
	}
	
	/** Get a list of exempted users
	 * @return a list of exempted users
	 */
	public List<Player> getExemptedUsers() {
		return exemptedUsers;
	}
}