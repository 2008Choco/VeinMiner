package me.choco.veinminer.utils;

import com.google.common.base.Preconditions;

import org.bukkit.block.Block;

/**
 * Represents all possible relative directions of a block supported by VeinMiner
 */
public enum VBlockFace {

	// CORE DIRECTIONS
	NORTH(0, 0, -1), 
	EAST(1, 0, 0), 
	SOUTH(0, 0, 1), 
	WEST(-1, 0, 0), 
	UP(0, 1, 0), 
	DOWN(0, -1, 0),
	
	// CORNER DIRECTIONS
	NORTH_EAST(1, 0, -1),
	NORTH_WEST(-1, 0, -1),
	SOUTH_EAST(1, 0, 1),
	SOUTH_WEST(-1, 0, 1),
	
	// EDGE DIRECTIONS
	NORTH_UP(0, 1, -1), 
	EAST_UP(1, 1, 0), 
	SOUTH_UP(0, 1, 1), 
	WEST_UP(-1, 1, 0),
	NORTH_DOWN(0, -1, -1), 
	EAST_DOWN(1, -1, 0), 
	SOUTH_DOWN(0, -1, 1), 
	WEST_DOWN(-1, -1, 0),
	
	// CORNER EDGE DIRECTIONS
	NORTH_EAST_UP(1, 1, -1),
	NORTH_WEST_UP(-1, 1, -1),
	SOUTH_EAST_UP(1, 1, 1),
	SOUTH_WEST_UP(-1, 1, 1),
	NORTH_EAST_DOWN(1, -1, -1),
	NORTH_WEST_DOWN(-1, -1, -1),
	SOUTH_EAST_DOWN(1, -1, 1),
	SOUTH_WEST_DOWN(-1, -1, 1);

	private final int xTranslation, yTranslation, zTranslation;

	private VBlockFace(int xTranslation, int yTranslation, int zTranslation) {
		this.xTranslation = xTranslation;
		this.yTranslation = yTranslation;
		this.zTranslation = zTranslation;
	}

	/**
	 * Get the X value translation of this block face
	 * 
	 * @return the X translation
	 */
	public int getXTranslation() {
		return xTranslation;
	}

	/**
	 * Get the Y value translation of this block face
	 * 
	 * @return the Y translation
	 */
	public int getYTranslation() {
		return yTranslation;
	}

	/**
	 * Get the Z value translation of this block face
	 * 
	 * @return the Z translation
	 */
	public int getZTranslation() {
		return zTranslation;
	}
	
	/**
	 * Get the block relative to the specified block based on the current block face
	 * 
	 * @param block the block of reference
	 * @return the relative block
	 */
	public Block getRelative(Block block) {
		Preconditions.checkArgument(block != null, "Cannot get the relative block of a null block");
		return block.getWorld().getBlockAt(block.getX() + xTranslation, block.getY() + yTranslation, block.getZ() + zTranslation);
	}
	
	/* Block views:
	 * 
	 *       Arial:               Front:
	 * 
	 *   NW    N    NE          WU   UP    EU
	 *                         
	 *   W   BLOCK   E          W   BLOCK   E
	 *                         
	 *   SW    S    ES          WD  DOWN   ED
	 *   
	 */
}