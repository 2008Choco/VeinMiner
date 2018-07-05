package wtf.choco.veinminer.commands;

import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.api.PlayerSwitchPatternEvent;
import wtf.choco.veinminer.api.veinutils.VeinBlock;
import wtf.choco.veinminer.api.veinutils.VeinTool;
import wtf.choco.veinminer.pattern.VeinMiningPattern;
import wtf.choco.veinminer.utils.VeinMinerManager;

public class VeinMinerCmd implements CommandExecutor {
	
	private final VeinMiner plugin;
	private final VeinMinerManager manager;
	
	public VeinMinerCmd(VeinMiner plugin) {
		this.plugin = plugin;
		this.manager = plugin.getVeinMinerManager();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (args.length == 0) {
			this.sendMessage(sender, "/veinminer <reload|version|blocklist|toggle|pattern>");
			return true;
		}
		
		// Reload subcommand
		if (args[0].equalsIgnoreCase("reload")) {
			if (!sender.hasPermission("veinminer.reload")) {
				this.sendMessage(sender, "You don't have the sufficient permissions to run this command");
				return true;
			}
			
			this.plugin.reloadConfig();
			this.manager.loadVeinableBlocks();
			this.manager.loadDisabledWorlds();
			this.manager.loadMaterialAliases();
			
			this.sendMessage(sender, ChatColor.GREEN + "Configuration Successfully Reloaded");
		}
		
		// Version subcommand
		else if (args[0].equalsIgnoreCase("version")) {
			sender.sendMessage(ChatColor.GOLD + "--------------------------------------------");
			sender.sendMessage("");
			sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Version: " + ChatColor.RESET + ChatColor.GRAY + plugin.getDescription().getVersion());
			sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Developer / Maintainer: " + ChatColor.RESET + ChatColor.GRAY + "2008Choco");
			sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Development Page: " + ChatColor.RESET + ChatColor.GRAY + "https://www.spigotmc.org/resources/vein-miner.12038/");
			sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Report Bugs To: " + ChatColor.RESET + ChatColor.GRAY + "https://github.com/2008Choco/VeinMiner/issues/");
			sender.sendMessage("");
			sender.sendMessage(ChatColor.GOLD + "--------------------------------------------");
		}
		
		// Toggle subcommand
		else if (args[0].equalsIgnoreCase("toggle")) {
			if (!(sender instanceof Player)) {
				this.sendMessage(sender, "Cannot toggle VeinMiner for the console. You're not a player, silly!");
				return true;
			}
			
			Player player = (Player) sender;
			if (!canVeinMine(player)) {
				this.sendMessage(player, "You can't toggle a feature you do not have access to, silly!");
				return true;
			}
			
			if (!player.hasPermission("veinminer.toggle")) {
				this.sendMessage(player, "You don't have the sufficient permissions to run this command");
				return true;
			}
			
			// TOGGLE A SPECIFIC TOOL
			if (args.length >= 2) {
				VeinTool tool = VeinTool.getByName(args[1]);
				if (tool == null) {
					this.sendMessage(sender, "Invalid tool name, \"" + ChatColor.AQUA + args[1] + ChatColor.GRAY + "\"");
					return true;
				}
				
				tool.toggleVeinMiner(player);
				this.sendMessage(player, "VeinMiner successfully toggled "
						+ (tool.hasVeinMinerDisabled(player) ? ChatColor.RED + "off" : ChatColor.GREEN + "on")
						+ ChatColor.GRAY + " for tool " + tool.name());
			}
			
			// TOGGLE ALL TOOLS
			else {
				boolean hasAllDisabled = true;
				for (VeinTool tool : VeinTool.values()) {
					if (tool.hasVeinMinerEnabled(player)) {
						hasAllDisabled = false;
						break;
					}
				}
				
				for (VeinTool tool : VeinTool.values()) {
					tool.toggleVeinMiner(player, hasAllDisabled);
				}
				
				this.sendMessage(player, "VeinMiner successfully toggled "
						+ (hasAllDisabled ? ChatColor.GREEN + "on" : ChatColor.RED + "off")
						+ ChatColor.GRAY + " for all tools!");
			}
		}
		
		// Blocklist subcommand
		else if (args[0].equalsIgnoreCase("blocklist")) {
			if (args.length < 2) {
				this.sendMessage(sender, "/veinminer blocklist <tool> <add|remove|list>");
				return true;
			}
			
			VeinTool tool = VeinTool.getByName(args[1]);
			
			if (tool == null) {
				this.sendMessage(sender, "Invalid tool name, \"" + ChatColor.AQUA + args[1] + ChatColor.GRAY + "\"");
				return true;
			}
			
			if (args.length < 3) {
				this.sendMessage(sender, "/veinminer blocklist " + args[1] + " <add|remove|list>");
				return true;
			}
			
			// /veinminer blocklist <tool> add
			if (args[2].equalsIgnoreCase("add")) {
				if (!sender.hasPermission("veinminer.blocklist.add")) {
					this.sendMessage(sender, "You don't have the sufficient permissions to run this command");
					return true;
				}
				
				if (args.length < 4) {
					this.sendMessage(sender, "/veinminer blocklist add <block>[data]");
					return true;
				}
				
				// Get block data from command parameter
				BlockData data;
				boolean specificData = false;
				try {
					data = Bukkit.createBlockData(args[3]);
					specificData = args[3].contains("[");
				} catch (IllegalArgumentException e) {
					this.sendMessage(sender, "Unknown block type (was it an item?) and/or block states. " + args[3]);
					return true;
				}
				
				Material material = data.getMaterial();
				List<String> blocklist = plugin.getConfig().getStringList("BlockList." + tool.getName());
				
				if (VeinBlock.isVeinable(tool, material, specificData ? data : null)) {
					this.sendMessage(sender, "Block Id " + data.getAsString() + " is already on the list");
					return true;
				}
				
				blocklist.add(data.getAsString());
				this.plugin.getConfig().set("BlockList." + tool.getName(), blocklist);
				this.plugin.saveConfig();
				this.plugin.reloadConfig();
				
				VeinBlock.registerVeinminableBlock(material, specificData ? data : null, tool);
				this.sendMessage(sender, "Block Id " + data.getAsString() + " successfully added to the list");
			}
			
			// /veinminer blocklist <tool> remove
			else if (args[2].equalsIgnoreCase("remove")) {
				if (!sender.hasPermission("veinminer.blocklist.remove")) {
					this.sendMessage(sender, "You don't have the sufficient permissions to run this command");
					return true;
				}
				
				if (args.length < 4) {
					this.sendMessage(sender, "/veinminer blocklist remove <block>[data]");
					return true;
				}
				
				// Get block data from command parameter
				BlockData data;
				boolean specificData = false;
				try {
					data = Bukkit.createBlockData(args[3]);
					specificData = args[3].contains("[");
				} catch (IllegalArgumentException e) {
					this.sendMessage(sender, "Unknown block type (was it an item?) and/or block states. " + args[3]);
					return true;
				}
				
				Material material = data.getMaterial();
				List<String> blocklist = plugin.getConfig().getStringList("BlockList." + tool.getName());
				
				if (!VeinBlock.isVeinable(tool, material, specificData ? data : null)) {
					this.sendMessage(sender, "Block Id " + data.getAsString() + " is not on the list");
					return true;
				}
				
				blocklist.remove(data.getAsString());
				this.plugin.getConfig().set("BlockList." + tool.getName(), blocklist);
				this.plugin.saveConfig();
				this.plugin.reloadConfig();
				
				VeinBlock block = VeinBlock.getVeinminableBlock(material, specificData ? data : null);
				block.removeMineableBy(tool);
				this.sendMessage(sender, "Block Id " + data.getAsString() + " successfully removed from the list");
			}
			
			// /veinminer blocklist <tool> list
			else if (args[2].equalsIgnoreCase("list")) {
				if (!sender.hasPermission("veinminer.blocklist.list." + tool.getName().toLowerCase())) {
					this.sendMessage(sender, "You don't have the sufficient permissions to list this tool");
					return true;
				}
				
				Set<VeinBlock> blocklist = VeinBlock.getVeinminableBlocks(tool);
				sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "VeinMiner Blocklist (Tool = " + tool + "): ");
				
				for (VeinBlock block : blocklist) {
					sender.sendMessage(ChatColor.YELLOW + "[*] " + (block.hasSpecficData() ? block.getData().getAsString() : block.getMaterial().name()));
				}
			}
			
			// Unknown parameter
			else {
				this.sendMessage(sender, "/veinminer blocklist " + args[1] + "<add|remove|list>");
				return true;
			}
		}
		
		else if (args[0].equalsIgnoreCase("pattern")) {
			if (!(sender instanceof Player)) {
				this.sendMessage(sender, "Only players are permitted to change their veinmining pattern");
				return true;
			}
			
			if (!sender.hasPermission("veinminer.pattern")) {
				this.sendMessage(sender, "You don't have sufficient permissions to run this command!");
				return true;
			}
			
			if (args.length < 2) {
				this.sendMessage(sender, "/veinminer pattern <pattern_id>");
				return true;
			}
			
			Player player = (Player) sender;
			String patternNamespace = args[1].toLowerCase();
			
			if (!patternNamespace.contains(":")) {
				patternNamespace = plugin.getName().toLowerCase() + ":" + patternNamespace;
			} else if (patternNamespace.startsWith(":") || patternNamespace.split(":").length > 2) {
				this.sendMessage(player, "Invalid namespace. Pattern IDs should be formatted as " + ChatColor.YELLOW + "namespace:id" + ChatColor.GRAY + " (i.e. " + ChatColor.YELLOW + "\"veinminer:default\"" + ChatColor.GRAY + ")");
				return true;
			}
			
			VeinMiningPattern pattern = plugin.getPatternRegistry().getPattern(patternNamespace);
			if (pattern == null) {
				this.sendMessage(player, "Unknown pattern found with ID " + ChatColor.YELLOW + patternNamespace + ChatColor.GRAY + ". Perhaps you meant \"default\"");
				return true;
			}
			
			PlayerSwitchPatternEvent pspe = new PlayerSwitchPatternEvent(player, manager.getPatternFor(player), pattern);
			Bukkit.getPluginManager().callEvent(pspe);
			
			this.manager.setPattern(player, pattern);
			this.sendMessage(player, ChatColor.GREEN + "Pattern successfully changed to " + ChatColor.YELLOW + patternNamespace);
		}
		
		// Unknown command usage
		else {
			this.sendMessage(sender, "/veinminer <reload|version|blocklist|toggle|pattern>");
			return true;
		}
		
		return true;
	}
	
	private void sendMessage(CommandSender sender, String message) {
		sender.sendMessage(ChatColor.BLUE + "VeinMiner" + ChatColor.DARK_BLUE + "> " + ChatColor.GRAY + message);
	}
	
	private boolean canVeinMine(Player player) {
		if (player.hasPermission("veinminer.veinmine.*")) return true;
		
		for (VeinTool tool : VeinTool.values())
			if (player.hasPermission("veinminer.veinmine." + tool.getName().toLowerCase())) return true;
		return false;
	}
}