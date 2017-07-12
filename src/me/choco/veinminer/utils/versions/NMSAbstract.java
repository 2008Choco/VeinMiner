package me.choco.veinminer.utils.versions;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.choco.veinminer.VeinMiner;

/** 
 * Used to allow for version independent code. Not intended for public use, but may
 * be used if absolutely necessary
 * 
 * @see VeinMiner#getNMSAbstract()
 */
public interface NMSAbstract {
	
	/** 
	 * Force a player to break a block
	 * 
	 * @param player the player to force
	 * @param block the block to break
	 */
	public void breakBlock(Player player, Block block);
	
	/** 
	 * Get the item in hand for a player
	 * 
	 * @param player the player
	 * @return the item in hand
	 */
	public ItemStack getItemInHand(Player player);
	
}