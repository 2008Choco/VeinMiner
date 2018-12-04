package wtf.choco.veinminer.api;

import java.util.function.Predicate;

import com.google.common.base.Predicates;

import org.bukkit.entity.Player;

/**
 * Represents the different methods of activating VeinMiner.
 */
public enum ActivationStrategy {

	/**
	 * Activated when a Player is holding sneak.
	 */
	SNEAK(Player::isSneaking),

	/**
	 * Activated when a Player is standing up (i.e. not sneaking).
	 */
	STAND(Predicates.not(Player::isSneaking)),

	/**
	 * Always activated.
	 */
	ALWAYS(Predicates.alwaysTrue());

	private final Predicate<Player> condition;

	private ActivationStrategy(Predicate<Player> condition) {
		this.condition = condition;
	}

	/**
	 * Check whether a Player is capable of vein mining according to this activation.
	 *
	 * @param player the player to check
	 *
	 * @return true if valid to vein mine, false otherwise
	 */
	public boolean isValid(Player player) {
		return player != null && this.condition.test(player);
	}

	/**
	 * Get a MineActivation based on its name.
	 *
	 * @param name the name for which to search. Case insensitive
	 *
	 * @return the resulting activation. null if none found
	 */
	public static ActivationStrategy getByName(String name) {
		for (ActivationStrategy activation : values())
			if (activation.name().equalsIgnoreCase(name)) return activation;
		return null;
	}

}