package me.choco.veinminer.anticheat;

import org.bukkit.entity.Player;

/**
 * Represents a hook for an anticheat plugin. Implementations of this hook should
 * exempt and unexempt players in the respective methods
 */
public interface AntiCheatHook {
	
	/**
	 * Get the name of the plugin representing this hook
	 * 
	 * @return this plugin hook
	 */
	public String getPluginName();
	
	/**
	 * Exempt a player from a fast-break check in the hooked anticheat
	 * 
	 * @param player the player to exempt
	 */
	public void exempt(Player player);
	
	/**
	 * Unexempt a player from a fast-break check in the hooked anticheat
	 * 
	 * @param player the player to unexempt
	 */
	public void unexempt(Player player);
	
	/**
	 * Check whether the provided player should be unexempted. This is a special-case
	 * method used to check if players should be unexempted under certain situations.
	 * For example, in the case of {@link AntiCheatHookNCP}, this method returns false
	 * if the player was exempted prior to the execution of {@link #exempt(Player)}
	 * and should not be unexempted
	 * 
	 * @param player the player to check
	 * @return true if should unexempt, false otherwise
	 */
	public default boolean shouldUnexempt(Player player) {
		return true;
	}
	
	/**
	 * Check whether this anticheat hook is supported or not. This should return false
	 * if, for example in AntiAura, an API method to exempt players was added in a
	 * later version of the anticheat plugin
	 * 
	 * @return true if supported, false otherwise
	 */
	public default boolean isSupported() {
		return true;
	}
	
}