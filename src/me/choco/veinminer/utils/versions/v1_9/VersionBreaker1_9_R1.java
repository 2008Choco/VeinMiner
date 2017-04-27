package me.choco.veinminer.utils.versions.v1_9;

import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.choco.veinminer.utils.versions.VersionBreaker;
import net.minecraft.server.v1_9_R1.BlockPosition;

/**
 * The Minecraft 1.9.0 - 1.9.3 implementation of {@link VersionBreaker}. This class should
 * not be instantiated anywhere other than VeinMiner's main class
 */
public class VersionBreaker1_9_R1 implements VersionBreaker {

	@Override
	public void breakBlock(Player player, Block block) {
		((CraftPlayer) player).getHandle().playerInteractManager.breakBlock(new BlockPosition(block.getX(), block.getY(), block.getZ()));
	}

	@Override
	public ItemStack getItemInHand(Player player) {
		return player.getInventory().getItemInMainHand();
	}
}