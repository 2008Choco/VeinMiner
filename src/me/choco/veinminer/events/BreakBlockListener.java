package me.choco.veinminer.events;

import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.Sets;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import me.choco.veinminer.VeinMiner;
import me.choco.veinminer.api.PlayerVeinMineEvent;
import me.choco.veinminer.api.veinutils.MaterialAlias;
import me.choco.veinminer.api.veinutils.VeinBlock;
import me.choco.veinminer.api.veinutils.VeinTool;
import me.choco.veinminer.utils.ConfigOption;
import me.choco.veinminer.utils.VBlockFace;
import me.choco.veinminer.utils.VeinMinerManager;
import me.choco.veinminer.utils.versions.NMSAbstract;

public class BreakBlockListener implements Listener {
	
	private final Set<Block> blocks = Sets.newHashSet(), blocksToAdd = Sets.newHashSet();

	private final VeinMiner plugin;
	private final VeinMinerManager manager;
	private final NMSAbstract nmsAbstract;
	
	public BreakBlockListener(VeinMiner plugin){
		this.plugin = plugin;
		this.manager = plugin.getVeinMinerManager();
		this.nmsAbstract = plugin.getNMSAbstract();
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	private void onBlockBreak(BlockBreakEvent event){
		if (!event.getClass().equals(BlockBreakEvent.class)) return; // For plugins such as McMMO, who fire custom events
		if (blocks.contains(event.getBlock())) return;
		Block block = event.getBlock();
		
		Player player = event.getPlayer();
		ItemStack itemUsed = nmsAbstract.getItemInHand(player);
		if (itemUsed == null) return;
		
		// VeinTool used check
		VeinTool tool = VeinTool.fromMaterial(itemUsed.getType());
		if (tool == null) tool = VeinTool.ALL;
		
		// Invalid player state check
		if (manager.isDisabledInWorld(block.getWorld())) return;
		if ((player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE)) return;
		if (!player.hasPermission("veinminer.veinmine." + tool.getName().toLowerCase())) return;
		if ((!VeinBlock.isVeinable(tool, block.getType(), block.getData()) 
				&& !(VeinBlock.isVeinable(VeinTool.ALL, block.getType(), block.getData()) && player.hasPermission("veinminer.veinmine.all")))) return;
		if (tool.hasVeinMinerDisabled(player)) return;
		if (!canActivate(player)) return;
		
		// TIME TO VEINMINE
		blocks.add(block);
		int maxVeinSize = tool.getMaxVeinSize();
		MaterialAlias alias = this.manager.getAliasFor(block.getType(), block.getData());
		
		// New VeinMiner algorithm- Allocate blocks to break
		while (blocks.size() <= maxVeinSize) {
			Iterator<Block> trackedBlocks = blocks.iterator();
			while (trackedBlocks.hasNext() && blocks.size() + blocksToAdd.size() <= maxVeinSize){
				Block b = trackedBlocks.next();
				for (VBlockFace face : ConfigOption.FACES_TO_MINE){
					if (blocks.size() + blocksToAdd.size() >= maxVeinSize) break;
					
					Block nextBlock = face.getRelative(b);
					if (blocks.contains(nextBlock) || !blockIsSameMaterial(block, nextBlock, alias)) 
						continue;
					
					blocksToAdd.add(nextBlock);
				}
			}
			
			blocks.addAll(blocksToAdd);
			if (blocksToAdd.size() == 0) break;
			blocksToAdd.clear();
		}
		
		// Fire a new PlayerVeinMineEvent
		PlayerVeinMineEvent vmEvent = new PlayerVeinMineEvent(player, VeinBlock.getVeinminableBlock(block.getType(), block.getData()), blocks);
		Bukkit.getPluginManager().callEvent(vmEvent);
		if (vmEvent.isCancelled()){
			this.blocks.clear();
			return;
		}
		
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
		
		if (plugin.isAntiAuraEnabled())
			AntiAuraAPI.API.toggleExemptFromFastBreak(player);
		/* Anti Cheat support end */
		
		// Actually destroying the allocated blocks
		boolean usesDurability = tool.usesDurability();
		int maxDurability = itemUsed.getType().getMaxDurability() - (ConfigOption.REPAIR_FRIENDLY_VEINMINER ? 1 : 0);
		for (Block b : blocks){
			short priorDurability = itemUsed.getDurability();
			nmsAbstract.breakBlock(player, b);
			short newDurability = itemUsed.getDurability();
			
			// Unbreaking enchantment precaution
			if (!usesDurability && priorDurability < newDurability)
				itemUsed.setDurability((short) (newDurability - 1));
			
			// Durability check
			if (newDurability >= maxDurability) break;
		}
		
		this.blocks.clear();
		
		// VEINMINER - DONE
		
		/* Anti Cheat Support ... Check if need to unexempt, in case they had been exempted prior to VeinMining */
		if (plugin.isNCPEnabled())
			if (unexemptNCP) NCPExemptionManager.unexempt(player, CheckType.BLOCKBREAK);
		if (plugin.isAACEnabled())
			plugin.getAntiCheatSupport().unexemptFromViolation(player);
		if (plugin.isAntiAuraEnabled())
			AntiAuraAPI.API.toggleExemptFromFastBreak(player);
	}
	
	@SuppressWarnings("deprecation")
	private boolean blockIsSameMaterial(Block original, Block block, MaterialAlias alias) {
		if (original.getType() == block.getType() && original.getData() == block.getData()) return true;
		
		// Check instead for aliases
		return alias != null && alias.isAliased(block.getType(), block.getData());
	}
	
	private boolean canActivate(Player player){
		String mode = ConfigOption.ACTIVATION_MODE;
		return (
				(mode.equalsIgnoreCase("SNEAK") && player.isSneaking()) ||
				(mode.equalsIgnoreCase("STAND") && !player.isSneaking())
			);
	}
}