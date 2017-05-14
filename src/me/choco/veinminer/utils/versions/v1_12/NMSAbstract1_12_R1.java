package me.choco.veinminer.utils.versions.v1_12;

import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.minecraft.server.v1_12_R1.BlockPosition;

import me.choco.veinminer.utils.versions.NMSAbstract;

/**
 * The Minecraft 1.12.0-Pre2+ implementation of {@link NMSAbstract}. This class should
 * not be instantiated anywhere other than VeinMiner's main class
 */
public class NMSAbstract1_12_R1 implements NMSAbstract {

	@Override
	public void breakBlock(Player player, Block block) {
		((CraftPlayer) player).getHandle().playerInteractManager.breakBlock(new BlockPosition(block.getX(), block.getY(), block.getZ()));
	}

	@Override
	public ItemStack getItemInHand(Player player) {
		return player.getInventory().getItemInMainHand();
	}
	
}
