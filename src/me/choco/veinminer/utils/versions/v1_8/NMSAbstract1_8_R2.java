package me.choco.veinminer.utils.versions.v1_8;

import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.choco.veinminer.utils.versions.NMSAbstract;
import net.minecraft.server.v1_8_R2.BlockPosition;

/**
 * The Minecraft 1.8.3 implementation of {@link NMSAbstract}. This class should
 * not be instantiated anywhere other than VeinMiner's main class
 */
public class NMSAbstract1_8_R2 implements NMSAbstract {

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
