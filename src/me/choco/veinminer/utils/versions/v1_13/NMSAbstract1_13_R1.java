package me.choco.veinminer.utils.versions.v1_13;

import com.google.common.base.Preconditions;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import me.choco.veinminer.utils.versions.NMSAbstract;
import me.choco.veinminer.utils.versions.v1_13.BlockPosition;
import me.choco.veinminer.utils.versions.v1_13.CraftPlayer;

/**
 * The Minecraft 1.13.0-Pre1+ implementation of {@link NMSAbstract}. This class should
 * not be instantiated anywhere other than VeinMiner's main class
 */
public class NMSAbstract1_13_R1 implements NMSAbstract {
	
	@Override
	public void breakBlock(Player player, Block block) {
		Preconditions.checkArgument(player != null, "A null player is incapable of breaking blocks");
		Preconditions.checkArgument(block != null, "Cannot break a null block");
		
		((CraftPlayer) player).getHandle().playerInteractManager.breakBlock(new BlockPosition(block.getX(), block.getY(), block.getZ()));
	}
	
}