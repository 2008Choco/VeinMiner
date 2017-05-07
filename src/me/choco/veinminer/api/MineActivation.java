package me.choco.veinminer.api;

import java.util.function.Predicate;

import org.bukkit.entity.Player;

/**
 * Represents the different methods of activating VeinMiner
 */
public enum MineActivation {
	
	/**
	 * Activated when a Player is holding sneak
	 */
	SNEAK("Sneak", Player::isSneaking),
	
	/**
	 * Activated when a Player is standing up (not sneaking)
	 */
	STAND("Stand", p -> !p.isSneaking());
	
	private final String label;
	private final Predicate<Player> condition;
	
	private MineActivation(String label, Predicate<Player> condition) {
		this.label = label;
		this.condition = condition;
	}
	
	/**
	 * Check whether a Player is capable of vein mining based on this activation
	 * 
	 * @param player the player to check
	 * @return true if valid to vein mine
	 */
	public boolean isValid(Player player) {
		return this.condition.test(player);
	}
	
	/**
	 * Get a MineActivation based on its name
	 * 
	 * @param name the name to search for
	 * @return the resulting activation. null if none found
	 */
	public static MineActivation getByName(String name) {
		for (MineActivation activation : values())
			if (activation.label.equals(name)) return activation;
		return null;
	}
	
}