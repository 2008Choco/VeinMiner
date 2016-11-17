package me.choco.veinminer.utils.versions;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class VersionBreakerDefault implements VersionBreaker {
	
	@Override
	public void breakBlock(Player player, Block block) {
		block.breakNaturally(this.getItemInHand(player));
	}
	
	@Override
	public ItemStack getItemInHand(Player player) {
		return player.getInventory().getItem(0);
	}
}