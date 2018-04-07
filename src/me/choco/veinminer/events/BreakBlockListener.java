package me.choco.veinminer.events;

import java.util.List;

import me.choco.veinminer.VeinMiner;
import me.choco.veinminer.anticheat.AntiCheatHook;
import me.choco.veinminer.api.MineActivation;
import me.choco.veinminer.api.PlayerVeinMineEvent;
import me.choco.veinminer.api.veinutils.MaterialAlias;
import me.choco.veinminer.api.veinutils.VeinBlock;
import me.choco.veinminer.api.veinutils.VeinTool;
import me.choco.veinminer.pattern.VeinMiningPattern;
import me.choco.veinminer.utils.NonNullArrayList;
import me.choco.veinminer.utils.VeinMinerManager;
import me.choco.veinminer.utils.metrics.StatTracker;
import me.choco.veinminer.utils.versions.NMSAbstract;

import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class BreakBlockListener implements Listener {
	
	private final List<Block> blocks = new NonNullArrayList<>();

	private final VeinMiner plugin;
	private final VeinMinerManager manager;
	private final NMSAbstract nmsAbstract;
	private final StatTracker statTracker;
	
	public BreakBlockListener(VeinMiner plugin) {
		this.plugin = plugin;
		this.manager = plugin.getVeinMinerManager();
		this.nmsAbstract = plugin.getNMSAbstract();
		this.statTracker = plugin.getStatTracker();
	}
	
	@EventHandler
	private void onBlockBreak(BlockBreakEvent event) {
		if (!event.getClass().equals(BlockBreakEvent.class)) return; // For plugins such as McMMO, who fire custom events
		if (blocks.contains(event.getBlock())) return;
		
		Block block = event.getBlock();
		if (manager.isDisabledInWorld(block.getWorld())) return;
		
		Player player = event.getPlayer();
		ItemStack itemUsed = event.getPlayer().getInventory().getItemInMainHand();
		
		// VeinTool used check
		VeinTool tool = (itemUsed != null) ? VeinTool.fromMaterial(itemUsed.getType()) : VeinTool.ALL;
		
		// Activation check
		MineActivation activation = EnumUtils.getEnum(MineActivation.class, plugin.getConfig().getString("ActivationMode", "SNEAK").toUpperCase());
		if (activation == null) activation = MineActivation.SNEAK;
		
		// Invalid player state check
		if (!activation.isValid(player) || player.getGameMode() != GameMode.SURVIVAL) return;
		if (!player.hasPermission("veinminer.veinmine." + tool.getName().toLowerCase())) return;
		if (tool.hasVeinMinerDisabled(player)) return;
		if ((!VeinBlock.isVeinable(tool, block.getType(), block.getBlockData())
				&& !(VeinBlock.isVeinable(VeinTool.ALL, block.getType(), block.getBlockData()) && player.hasPermission("veinminer.veinmine.all")))) return;
		
		// TIME TO VEINMINE
		MaterialAlias alias = this.manager.getAliasFor(block.getType());
		if (alias == null) alias = this.manager.getAliasFor(block.getType(), block.getBlockData());
		
		this.blocks.add(block);
		VeinMiningPattern pattern = manager.getPatternFor(player);
		pattern.allocateBlocks(blocks, block, tool, alias);
		
		// Fire a new PlayerVeinMineEvent
		PlayerVeinMineEvent vmEvent = new PlayerVeinMineEvent(player, VeinBlock.getVeinminableBlock(block.getType(), block.getBlockData()), tool, blocks, pattern);
		Bukkit.getPluginManager().callEvent(vmEvent);
		if (vmEvent.isCancelled()) {
			this.blocks.clear();
			return;
		}
		
		// Anticheat support
		List<AntiCheatHook> hooks = plugin.getAnticheatHooks();
		hooks.stream().filter(AntiCheatHook::isSupported).forEach(h -> h.exempt(player));
		
		// Actually destroying the allocated blocks
		boolean usesDurability = tool.usesDurability();
		int maxDurability = itemUsed.getType().getMaxDurability() - (plugin.getConfig().getBoolean("RepairFriendlyVeinMiner", false) ? 1 : 0);
		for (Block b : blocks) {
			short priorDurability = itemUsed.getDurability();
			if (priorDurability >= maxDurability) break;
			
			this.nmsAbstract.breakBlock(player, b);
			this.statTracker.accumulateVeinMinedMaterial(b.getType());
			
			// Unbreaking enchantment precaution
			short newDurability = itemUsed.getDurability();
			if (!usesDurability && priorDurability < newDurability)
				itemUsed.setDurability((short) (newDurability - 1));
		}
		
		this.blocks.clear();
		
		// VEINMINER - DONE
		
		// Unexempt from anticheats
		hooks.stream()
			.filter(AntiCheatHook::isSupported)
			.filter(h -> h.shouldUnexempt(player))
			.forEach(h -> h.unexempt(player));
	}
	
}