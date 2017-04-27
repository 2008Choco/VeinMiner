package me.choco.veinminer.utils.versions.v1_8;

import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.minecraft.server.v1_8_R1.BlockPosition;

import me.choco.veinminer.utils.versions.VersionBreaker;

/**
 * The Minecraft 1.8.0 - 1.8.2 implementation of {@link VersionBreaker}. This class should
 * not be instantiated anywhere other than VeinMiner's main class
 */
public class VersionBreaker1_8_R1 implements VersionBreaker {

	@Override
	public void breakBlock(Player player, Block block) {
		((CraftPlayer) player).getHandle().playerInteractManager.breakBlock(new BlockPosition(block.getX(), block.getY(), block.getZ()));
	}

	@SuppressWarnings("deprecation")
	@Override
	public ItemStack getItemInHand(Player player) {
		return player.getItemInHand();
	}
}