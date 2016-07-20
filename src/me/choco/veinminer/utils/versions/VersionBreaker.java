package me.choco.veinminer.utils.versions;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Used to allow for version independent code. 
 * <br>This interface is ONLY to be used within VeinMiner's code. This should not be used
 * for public API usage, as it is not a class that comes with any pre-built code.
 * <br>
 * <br>Utilization of VeinMiner's instantiated VersionBreaker is permitted.
 * 
 * @see {@link VeinMiner#getVersionBreaker()}
 */
public interface VersionBreaker {
	
	/** Force a player to break a block
	 * @param player - The player to force
	 * @param block - The block to break
	 */
	public void breakBlock(Player player, Block block);
	
	/** Get the item in hand for a player
	 * @param player - The player
	 * @return the item in hand
	 */
	public ItemStack getItemInHand(Player player);
	
}