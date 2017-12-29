package me.choco.veinminer.events;

import java.util.List;

import org.apache.commons.lang3.EnumUtils;
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
import me.choco.veinminer.api.MineActivation;
import me.choco.veinminer.api.PlayerVeinMineEvent;
import me.choco.veinminer.api.veinutils.MaterialAlias;
import me.choco.veinminer.api.veinutils.VeinBlock;
import me.choco.veinminer.api.veinutils.VeinTool;
import me.choco.veinminer.pattern.VeinMiningPattern;
import me.choco.veinminer.utils.NonNullArrayList;
import me.choco.veinminer.utils.VeinMinerManager;
import me.choco.veinminer.utils.versions.NMSAbstract;

public class BreakBlockListener implements Listener {
	
	private final List<Block> blocks = new NonNullArrayList<>();

	private final VeinMiner plugin;
	private final VeinMinerManager manager;
	private final NMSAbstract nmsAbstract;
	
	public BreakBlockListener(VeinMiner plugin) {
		this.plugin = plugin;
		this.manager = plugin.getVeinMinerManager();
		this.nmsAbstract = plugin.getNMSAbstract();
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	private void onBlockBreak(BlockBreakEvent event) {
		if (!event.getClass().equals(BlockBreakEvent.class)) return; // For plugins such as McMMO, who fire custom events
		if (blocks.contains(event.getBlock())) return;
		Block block = event.getBlock();
		
		Player player = event.getPlayer();
		ItemStack itemUsed = nmsAbstract.getItemInHand(player);
		if (itemUsed == null) return;
		
		// VeinTool used check
		VeinTool tool = VeinTool.fromMaterial(itemUsed.getType());
		if (tool == null) tool = VeinTool.ALL;
		
		// Activation check
		MineActivation activation = EnumUtils.getEnum(MineActivation.class, plugin.getConfig().getString("ActivationMode", "SNEAK").toUpperCase());
		if (activation == null) activation = MineActivation.SNEAK;
		
		// Invalid player state check
		if (!activation.isValid(player)) return;
		if (manager.isDisabledInWorld(block.getWorld())) return;
		if ((player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE)) return;
		if (!player.hasPermission("veinminer.veinmine." + tool.getName().toLowerCase())) return;
		if ((!VeinBlock.isVeinable(tool, block.getType(), block.getData()) 
				&& !(VeinBlock.isVeinable(VeinTool.ALL, block.getType(), block.getData()) && player.hasPermission("veinminer.veinmine.all")))) return;
		if (tool.hasVeinMinerDisabled(player)) return;
		
		// TIME TO VEINMINE
		MaterialAlias alias = this.manager.getAliasFor(block.getType());
		if (alias == null) alias = this.manager.getAliasFor(block.getType(), block.getData());
		
		this.blocks.add(block);
		VeinMiningPattern pattern = manager.getPatternFor(player);
		pattern.allocateBlocks(blocks, block, tool, alias);
		
		// Fire a new PlayerVeinMineEvent
		PlayerVeinMineEvent vmEvent = new PlayerVeinMineEvent(player, VeinBlock.getVeinminableBlock(block.getType(), block.getData()), tool, blocks);
		Bukkit.getPluginManager().callEvent(vmEvent);
		if (vmEvent.isCancelled()) {
			this.blocks.clear();
			return;
		}
		
		/* Anti Cheat support start */
		boolean unexemptNCP = false;
		if (plugin.isNCPEnabled()) {
			if (!NCPExemptionManager.isExempted(player, CheckType.BLOCKBREAK)) {
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
		int maxDurability = itemUsed.getType().getMaxDurability() - (plugin.getConfig().getBoolean("RepairFriendlyVeinMiner", false) ? 1 : 0);
		for (Block b : blocks) {
			short priorDurability = itemUsed.getDurability();
			if (priorDurability >= maxDurability) break;
			
			nmsAbstract.breakBlock(player, b);
			short newDurability = itemUsed.getDurability();
			
			// Unbreaking enchantment precaution
			if (!usesDurability && priorDurability < newDurability)
				itemUsed.setDurability((short) (newDurability - 1));
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
	
}