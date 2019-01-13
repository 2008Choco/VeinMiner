package wtf.choco.veinminer.pattern;

import org.bukkit.block.Block;

import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.api.VBlockFace;
import wtf.choco.veinminer.data.MaterialAlias;
import wtf.choco.veinminer.data.block.VeinBlock;

/**
 * Utility methods for {@link VeinMiningPattern} implementations.
 *
 * @author Parker Hawke - 2008Choco
 */
public final class PatternUtils {

	private static final VeinMiner VEINMINER = VeinMiner.getPlugin();
	private static final VBlockFace[] LIMITED_FACES = {
			VBlockFace.UP, VBlockFace.DOWN, VBlockFace.NORTH, VBlockFace.SOUTH, VBlockFace.EAST,
			VBlockFace.WEST, VBlockFace.NORTH_EAST, VBlockFace.NORTH_WEST, VBlockFace.SOUTH_EAST,
			VBlockFace.SOUTH_WEST
	};

	private PatternUtils() { }

	/**
	 * Check if a block is encapsulated by the VeinBlock type or considered aliased under
	 * the provided alias (if present).
	 *
	 * @param type the type for which to check
	 * @param alias the alias. null if no alias
	 * @param block the block to validate
	 *
	 * @return true if the provided block is of that type or aliased, false otherwise
	 */
	public static boolean isOfType(VeinBlock type, MaterialAlias alias, Block block) {
		return type.encapsulates(block) || (alias != null && alias.isAliased(block));
	}

	/**
	 * Get an array of VBlockFaces to mine based on VeinMiner's "IncludeEdges" configuration.
	 *
	 * @return the block face array
	 */
	public static VBlockFace[] getFacesToMine() {
		return VEINMINER.getConfig().getBoolean("IncludeEdges") ? VBlockFace.values() : LIMITED_FACES;
	}

}