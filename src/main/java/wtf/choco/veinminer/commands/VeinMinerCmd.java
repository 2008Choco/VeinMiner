package wtf.choco.veinminer.commands;

import static wtf.choco.veinminer.VeinMiner.CHAT_PREFIX;

import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.annotation.command.Command;
import org.bukkit.plugin.java.annotation.permission.ChildPermission;
import org.bukkit.plugin.java.annotation.permission.Permission;

import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.api.VeinMinerManager;
import wtf.choco.veinminer.api.VeinTool;
import wtf.choco.veinminer.api.blocks.VeinBlock;
import wtf.choco.veinminer.api.event.PlayerSwitchPatternEvent;
import wtf.choco.veinminer.pattern.VeinMiningPattern;
import wtf.choco.veinminer.utils.UpdateChecker;
import wtf.choco.veinminer.utils.UpdateChecker.UpdateResult;

@Permission(name = "veinminer.reload", desc = "Allow the use of the '/veinminer reload' subcommand")
@Permission(name = "veinminer.toggle", desc = "Allow the use of the '/veinminer toggle' subcommand", defaultValue = PermissionDefault.TRUE)
@Permission(name = "veinminer.pattern", desc = "Allow the use of the '/veinminer pattern' subcommand", defaultValue = PermissionDefault.TRUE)
@Permission(name = "veinminer.blocklist.list.*", desc = "Allow the use of the '/veinminer blocklist' subcommand", children = {
	@ChildPermission(name = "veinminer.blocklist.list.pickaxe"),
	@ChildPermission(name = "veinminer.blocklist.list.axe"),
	@ChildPermission(name = "veinminer.blocklist.list.shovel"),
	@ChildPermission(name = "veinminer.blocklist.list.hoe"),
	@ChildPermission(name = "veinminer.blocklist.list.shears"),
	@ChildPermission(name = "veinminer.blocklist.list.hand")
})
@Command(name = "veinminer", desc = "The main command for VeinMiner", usage = "/<command> <version|reload|blocklist|toggle|pattern>", aliases = {"vm"})
public class VeinMinerCmd implements CommandExecutor {

	private final VeinMiner plugin;
	private final VeinMinerManager manager;

	public VeinMinerCmd(VeinMiner plugin) {
		this.plugin = plugin;
		this.manager = plugin.getVeinMinerManager();
	}

	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Invalid command syntax! " + ChatColor.GRAY + "Missing parameter. " + ChatColor.YELLOW + "/veinminer <reload|version|blocklist|toggle|pattern>");
			return true;
		}

		// Reload subcommand
		if (args[0].equalsIgnoreCase("reload")) {
			if (!sender.hasPermission("veinminer.reload")) {
				sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "You have insufficient permissions to execute this command");
				return true;
			}

			this.plugin.reloadConfig();
			this.manager.loadVeinableBlocks();
			this.manager.loadDisabledWorlds();
			this.manager.loadMaterialAliases();

			sender.sendMessage(CHAT_PREFIX + ChatColor.GREEN + "VeinMiner configuration successfully reloaded");
		}

		// Version subcommand
		else if (args[0].equalsIgnoreCase("version")) {
			sender.sendMessage(ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + ChatColor.STRIKETHROUGH + "--------------------------------------------");
			sender.sendMessage("");
			sender.sendMessage(ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "Version: " + ChatColor.RESET + ChatColor.GRAY + plugin.getDescription().getVersion() + getUpdateSuffix());
			sender.sendMessage(ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "Developer: " + ChatColor.RESET + ChatColor.GRAY + "2008Choco " + ChatColor.YELLOW + "( https://choco.gg )");
			sender.sendMessage(ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "Development page: " + ChatColor.RESET + ChatColor.GRAY + "https://www.spigotmc.org/resources/veinminer.12038");
			sender.sendMessage(ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "Report bugs to: " + ChatColor.RESET + ChatColor.GRAY + "https://github.com/2008Choco/VeinMiner/issues");
			sender.sendMessage("");
			sender.sendMessage(ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + ChatColor.STRIKETHROUGH + "--------------------------------------------");
		}

		// Toggle subcommand
		else if (args[0].equalsIgnoreCase("toggle")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("VeinMiner cannot be toggled from the console...");
				return true;
			}

			Player player = (Player) sender;
			if (!canVeinMine(player)) {
				player.sendMessage(CHAT_PREFIX + ChatColor.RED + "You may not toggle a feature to which you do not have access");
				return true;
			}

			if (!player.hasPermission("veinminer.toggle")) {
				player.sendMessage(CHAT_PREFIX + ChatColor.RED + "You have insufficient permissions to execute this command");
				return true;
			}

			// Toggle a specific tool
			if (args.length >= 2) {
				VeinTool tool = VeinTool.getByName(args[1]);
				if (tool == null) {
					player.sendMessage(CHAT_PREFIX + "Invalid tool name: " + ChatColor.YELLOW + args[1]);
					return true;
				}

				tool.toggleVeinMiner(player);
				player.sendMessage(CHAT_PREFIX + "VeinMiner successfully toggled "
					+ (tool.hasVeinMinerDisabled(player) ? ChatColor.RED + "off" : ChatColor.GREEN + "on")
					+ ChatColor.GRAY + " for tool " + ChatColor.YELLOW + tool.getName().toLowerCase());
			}

			// Toggle all tools
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

				player.sendMessage(CHAT_PREFIX + "VeinMiner successfully toggled "
					+ (hasAllDisabled ? ChatColor.GREEN + "on" : ChatColor.RED + "off")
					+ ChatColor.GRAY + " for " + ChatColor.YELLOW + "all tools");
			}
		}

		// Blocklist subcommand
		else if (args[0].equalsIgnoreCase("blocklist")) {
			if (args.length < 2) {
				sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Invalid command syntax! " + ChatColor.GRAY + "Missing parameter(s). " + ChatColor.YELLOW + "/" + label + " blocklist <tool> <add|remove|list>");
				return true;
			}

			VeinTool tool = VeinTool.getByName(args[1]);

			if (tool == null) {
				sender.sendMessage(CHAT_PREFIX + "Invalid tool name: " + ChatColor.YELLOW + args[1]);
				return true;
			}

			if (args.length < 3) {
				sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Invalid command syntax! " + ChatColor.GRAY + "Missing parameter. " + ChatColor.YELLOW + "/" + label + " blocklist " + args[1] + " <add|remove|list>");
				return true;
			}

			// /veinminer blocklist <tool> add
			if (args[2].equalsIgnoreCase("add")) {
				if (!sender.hasPermission("veinminer.blocklist.add")) {
					sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "You have insufficient permissions to execute this command");
					return true;
				}

				if (args.length < 4) {
					sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Invalid command syntax! " + ChatColor.GRAY + "Missing parameter. " + ChatColor.YELLOW + "/" + label + " blocklist " + args[1] + " add <block>[[data]]");
					return true;
				}

				// Get block data from command parameter
				BlockData data;
				boolean specificData = !args[3].endsWith("[]") && args[3].endsWith("]");
				try {
					data = Bukkit.createBlockData(args[3].toLowerCase());
				} catch (IllegalArgumentException e) {
					sender.sendMessage(CHAT_PREFIX + "Unknown block type and/or block states. (Was it an item?) Given: " + ChatColor.YELLOW + args[3]);
					return true;
				}

				List<String> blocklist = plugin.getConfig().getStringList("BlockList." + tool.getName());

				if (manager.isVeinmineableBy(data, tool)) {
					sender.sendMessage(CHAT_PREFIX + "A block with the ID " + ChatColor.YELLOW + args[3] + ChatColor.GRAY + " is already on the " + ChatColor.YELLOW + args[1].toLowerCase() + ChatColor.GRAY + " blocklist");
					return true;
				}

				if (specificData) {
					this.manager.registerVeinmineableBlock(data, args[3].toLowerCase(), tool);
				} else {
					this.manager.registerVeinmineableBlock(data.getMaterial(), tool);
				}

				blocklist.add(args[3]);
				this.plugin.getConfig().set("BlockList." + tool.getName(), blocklist);
				this.plugin.saveConfig();
				this.plugin.reloadConfig();

				sender.sendMessage(CHAT_PREFIX + "Block ID " + ChatColor.YELLOW + args[3] + ChatColor.GRAY + " successfully added to the list");
			}

			// /veinminer blocklist <tool> remove
			else if (args[2].equalsIgnoreCase("remove")) {
				if (!sender.hasPermission("veinminer.blocklist.remove")) {
					sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "You have insufficient permissions to execute this command");
					return true;
				}

				if (args.length < 4) {
					sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Invalid command syntax! " + ChatColor.GRAY + "Missing parameter. " + ChatColor.YELLOW + "/" + label + " blocklist " + args[1] + " remove <block>[[data]]");
					return true;
				}

				// Get block data from command parameter
				BlockData data;
				try {
					data = Bukkit.createBlockData(args[3]);
				} catch (IllegalArgumentException e) {
					sender.sendMessage(CHAT_PREFIX + "Unknown block type and/or block states. (Was it an item?) Given: " + ChatColor.YELLOW + args[3]);
					return true;
				}

				List<String> blocklist = plugin.getConfig().getStringList("BlockList." + tool.getName());

				if (!manager.isVeinmineableBy(data, tool)) {
					sender.sendMessage(CHAT_PREFIX + "No block with the ID " + ChatColor.YELLOW + args[3] + ChatColor.GRAY + " was found on the " + ChatColor.YELLOW + args[1].toLowerCase() + ChatColor.GRAY + " blocklist");
					return true;
				}

				VeinBlock block = manager.getVeinmineableBlock(data);
				block.setVeinmineableBy(tool, false);
				if (block.getVeinmineableBy().size() == 0) {
					this.manager.unregisterVeinmineableBlock(block);
				}

				blocklist.remove(args[3]);
				this.plugin.getConfig().set("BlockList." + tool.getName(), blocklist);
				this.plugin.saveConfig();
				this.plugin.reloadConfig();

				sender.sendMessage(CHAT_PREFIX + "Block ID " + ChatColor.YELLOW + args[3] + ChatColor.GRAY + " successfully removed from the list");
			}

			// /veinminer blocklist <tool> list
			else if (args[2].equalsIgnoreCase("list")) {
				if (!sender.hasPermission("veinminer.blocklist.list." + tool.getName().toLowerCase())) {
					sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "You have insufficient permissions to execute this command");
					return true;
				}

				Set<VeinBlock> blocklist = manager.getVeinmineableBlocks(tool);
				sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "VeinMiner Blocklist (Tool = " + tool + "): ");

				for (VeinBlock block : blocklist) {
					sender.sendMessage(ChatColor.YELLOW + "  - " + block.getRawData());
				}
			}

			// Unknown parameter
			else {
				sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Invalid command syntax! " + ChatColor.GRAY + "Unknown parameter " + ChatColor.AQUA + args[2] + ChatColor.GRAY + ". " + ChatColor.YELLOW + "/" + label + " blocklist " + args[1] + " <add|remove|list>");
				return true;
			}
		}

		else if (args[0].equalsIgnoreCase("pattern")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("VeinMiner patterns cannot be changed from the console...");
				return true;
			}

			if (!sender.hasPermission("veinminer.pattern")) {
				sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "You have insufficient permissions to execute this command");
				return true;
			}

			if (args.length < 2) {
				sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Invalid command syntax! " + ChatColor.GRAY + "Missing parameter. " + ChatColor.YELLOW + "/" + label + " pattern <pattern_id>");
				return true;
			}

			Player player = (Player) sender;
			String patternNamespace = args[1].toLowerCase();

			if (!patternNamespace.contains(":")) {
				patternNamespace = plugin.getName().toLowerCase() + ":" + patternNamespace;
			} else if (patternNamespace.startsWith(":") || patternNamespace.split(":").length > 2) {
				player.sendMessage(CHAT_PREFIX + "Invalid ID. Pattern IDs should be formatted as " + ChatColor.YELLOW + "namespace:id" + ChatColor.GRAY + " (i.e. " + ChatColor.YELLOW + "veinminer:default" + ChatColor.GRAY + ")");
				return true;
			}

			VeinMiningPattern pattern = plugin.getPatternRegistry().getPattern(patternNamespace);
			if (pattern == null) {
				player.sendMessage(CHAT_PREFIX + "A pattern with the ID " + ChatColor.YELLOW + patternNamespace + ChatColor.GRAY + " could not be found. Perhaps you meant " + ChatColor.YELLOW + "default" + ChatColor.GRAY + "?");
				return true;
			}

			PlayerSwitchPatternEvent pspe = new PlayerSwitchPatternEvent(player, manager.getPatternFor(player), pattern);
			Bukkit.getPluginManager().callEvent(pspe);

			this.manager.setPattern(player, pattern);
			player.sendMessage(CHAT_PREFIX + "Pattern successfully changed to " + ChatColor.YELLOW + patternNamespace);
		}

		// Unknown command usage
		else {
			sender.sendMessage(CHAT_PREFIX + ChatColor.RED + "Invalid command syntax! " + ChatColor.GRAY + "Unknown parameter " + ChatColor.AQUA + args[0] + ChatColor.GRAY + ". " + ChatColor.YELLOW + "/" + label + " <reload|version|blocklist|toggle|pattern>");
			return true;
		}

		return true;
	}

	private boolean canVeinMine(Player player) {
		if (player.hasPermission("veinminer.veinmine.*")) return true;

		for (VeinTool tool : VeinTool.values())
			if (player.hasPermission("veinminer.veinmine." + tool.getName().toLowerCase())) return true;
		return false;
	}

	private String getUpdateSuffix() {
		if (!plugin.getConfig().getBoolean("PerformUpdateChecks")) {
			return "";
		}

		UpdateResult result = UpdateChecker.get().getLastResult();
		return (result != null && result.requiresUpdate()) ? " (" + ChatColor.GREEN + ChatColor.BOLD + "UPDATE AVAILABLE!" + ChatColor.GRAY + ")" : "";
	}

}