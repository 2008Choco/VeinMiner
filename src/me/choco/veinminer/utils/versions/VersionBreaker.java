package me.choco.veinminer.utils.versions;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface VersionBreaker {
	
	public void breakBlock(Player player, Block block);
	
	public ItemStack getItemInHand(Player player);
	
}