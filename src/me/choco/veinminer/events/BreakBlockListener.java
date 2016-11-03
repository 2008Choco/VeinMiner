package me.choco.veinminer.events;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import me.choco.veinminer.VeinMiner;
import me.choco.veinminer.api.PlayerVeinMineEvent;
import me.choco.veinminer.api.veinutils.VeinBlock;
import me.choco.veinminer.api.veinutils.VeinTool;
import me.choco.veinminer.utils.VeinMinerManager;
import me.choco.veinminer.utils.versions.VersionBreaker;

public class BreakBlockListener implements Listener{
	
	private static final BlockFace[] faces = {
		BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST,
		BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.SOUTH_EAST, BlockFace.NORTH_EAST
	};
	
	private static final int MAX_ITERATIONS = 15;
	private Set<Block> blocks = new HashSet<>(), blocksToAdd = new HashSet<>();

	private VeinMiner plugin;
	private VeinMinerManager manager;
	private VersionBreaker breaker;
	public BreakBlockListener(VeinMiner plugin){
		this.plugin = plugin;
		this.manager = plugin.getVeinMinerManager();
		this.breaker = plugin.getVersionBreaker();
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	private void onBlockBreak(BlockBreakEvent event){
		if (!event.getClass().equals(BlockBreakEvent.class)) return;
		if (blocks.contains(event.getBlock())) return;
		Block eBlock = event.getBlock();
		
		Player player = event.getPlayer();
		if (breaker.getItemInHand(player) == null) return;
		
		// VeinTool used check
		ItemStack itemUsed = breaker.getItemInHand(player);
		VeinTool usedTool = VeinTool.fromMaterial(itemUsed.getType());
		if (usedTool == null) usedTool = VeinTool.ALL;
		
		// Invalid player state check
		if (manager.isDisabledInWorld(eBlock.getWorld())) return;
		if ((player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE)) return;
		if (!player.hasPermission("veinminer.veinmine." + usedTool.getName().toLowerCase())) return;
		if ((!manager.isVeinable(usedTool, eBlock.getType(), eBlock.getData()) 
				&& !(manager.isVeinable(VeinTool.ALL, eBlock.getType(), eBlock.getData()) && player.hasPermission("veinminer.veinmine.all")))) return;
		if (manager.hasVeinMinerDisabled(player, usedTool)) return;
		if (!isProperlySneaking(player)) return;
		
		// TIME TO VEINMINE
		Block initialBlock = eBlock;
		int maxVeinSize = usedTool.getMaxVeinSize();
		
		blocks.add(initialBlock);
		
		// New VeinMiner algorithm- Allocate blocks to break
		for (int i = 0; i < MAX_ITERATIONS; i++){
			Iterator<Block> trackedBlocks = blocks.iterator();
			while (trackedBlocks.hasNext() && blocks.size() + blocksToAdd.size() <= maxVeinSize){
				Block b = trackedBlocks.next();
				for (BlockFace face : faces){
					if (blocks.size() + blocksToAdd.size() >= maxVeinSize) break;
					
					Block nextBlock = b.getRelative(face);
					if (!blockIsSameMaterial(initialBlock, nextBlock)
							|| blocks.contains(nextBlock)) continue;
					blocksToAdd.add(nextBlock);
				}
			}
			
			blocks.addAll(blocksToAdd);
			blocksToAdd.clear();
			
			if (blocks.size() >= maxVeinSize) break;
		}
		
		// Fire a new PlayerVeinMineEvent
		PlayerVeinMineEvent vmEvent = new PlayerVeinMineEvent(player, new VeinBlock(initialBlock.getType(), initialBlock.getData()), blocks);
		Bukkit.getPluginManager().callEvent(vmEvent);
		if (vmEvent.isCancelled()) return;
		blocks = vmEvent.getBlocks(); //Just in case it's modified in the event
		
		/* Anti Cheat support start */
		boolean unexemptNCP = false;
		if (plugin.isNCPEnabled()){
			if (!NCPExemptionManager.isExempted(player, CheckType.BLOCKBREAK)){
				NCPExemptionManager.exemptPermanently(player, CheckType.BLOCKBREAK);
				unexemptNCP = true;
			}
		}
		if (plugin.isAACEnabled())
			plugin.getAntiCheatSupport().exemptFromViolation(player);
		/* Anti Cheat support end */
		
		// Actually destroying the allocated blocks
		boolean usesDurability = usedTool.usesDurability();
		for (Block b : blocks){
			if (b.equals(eBlock)) continue;
			
			int priorDurability = itemUsed.getDurability();
			breaker.breakBlock(player, b);
			
			// Unbreaking enchantment precaution
			if (!usesDurability && priorDurability < itemUsed.getDurability())
				itemUsed.setDurability((short) (itemUsed.getDurability() - 1));
		}
		
		blocks.clear();
		
		// VEINMINER - DONE
		
		/* Anti Cheat Support ... Check if need to unexempt, in case they had been exempted prior to VeinMining */
		if (plugin.isNCPEnabled())
			if (unexemptNCP) NCPExemptionManager.unexempt(player, CheckType.BLOCKBREAK);
		if (plugin.isAACEnabled())
			plugin.getAntiCheatSupport().unexemptFromViolation(player);
	}
	
	@SuppressWarnings("deprecation")
	private boolean blockIsSameMaterial(Block original, Block block) {
		Material originalType = original.getType(), blockType = block.getType();
		if (blockType.equals(Material.GLOWING_REDSTONE_ORE) || blockType.equals(Material.REDSTONE_ORE)){
			if ((blockType.equals(originalType))
					|| (blockType.equals(Material.REDSTONE_ORE) && originalType.equals(Material.GLOWING_REDSTONE_ORE))
					|| (blockType.equals(Material.GLOWING_REDSTONE_ORE) && originalType.equals(Material.REDSTONE_ORE))){
				return true;
			}
		}
		return (blockType.equals(originalType) && block.getData() == original.getData());
	}
	
	private boolean isProperlySneaking(Player player){
		String value = plugin.getConfig().getString("ActivationMode");
		return (
				(value.equalsIgnoreCase("SNEAK") && player.isSneaking()) ||
				(value.equalsIgnoreCase("STAND") && !player.isSneaking())
			);
	}
}