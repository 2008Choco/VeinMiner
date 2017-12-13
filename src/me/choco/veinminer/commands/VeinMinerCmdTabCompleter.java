package me.choco.veinminer.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import me.choco.veinminer.VeinMiner;
import me.choco.veinminer.api.veinutils.VeinTool;
import me.choco.veinminer.pattern.VeinMiningPattern;

public class VeinMinerCmdTabCompleter implements TabCompleter {
	
	private final VeinMiner plugin;
	
	public VeinMinerCmdTabCompleter(VeinMiner plugin) {
		this.plugin = plugin;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		List<String> values = new ArrayList<>();
		if (args.length == 1) {
			values.add("version");
			if (sender.hasPermission("veinminer.reload")) values.add("reload");
			if (sender.hasPermission("veinminer.toggle")) values.add("toggle");
			if (hasBlocklistPerms(sender)) values.add("blocklist");
			if (sender.hasPermission("veinminer.pattern")) values.add("pattern");
		}
		
		else if (args.length == 2) {
			if (args[0].equalsIgnoreCase("toggle") || args[0].equalsIgnoreCase("blocklist")) {
				for (VeinTool tool : VeinTool.values())
					values.add(tool.name().toLowerCase());
			}
			
			else if (args[0].equalsIgnoreCase("pattern")) {
				values = plugin.getPatternRegistry().getPatterns().stream()
						.map(VeinMiningPattern::getKey)
						.map(NamespacedKey::toString)
						.collect(Collectors.toList());
			}
		}
		
		else if (args.length == 3) {
			if (args[0].equalsIgnoreCase("blocklist")) {
				if (sender.hasPermission("veinminer.blocklist.add")) values.add("add");
				if (sender.hasPermission("veinminer.blocklist.remove")) values.add("remove");
				if (sender.hasPermission("veinminer.blocklist.list.*")) values.add("list");
			}
		}
		
		return values;
	}
	
	private boolean hasBlocklistPerms(CommandSender sender) {
		return (sender.hasPermission("veinminer.blocklist.add") || sender.hasPermission("veinminer.blocklist.remove")
				|| sender.hasPermission("veinminer.blocklist.list.*"));
	}
}