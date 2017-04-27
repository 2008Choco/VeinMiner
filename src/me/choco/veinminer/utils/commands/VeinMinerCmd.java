package me.choco.veinminer.utils.commands;

import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.choco.veinminer.VeinMiner;
import me.choco.veinminer.api.veinutils.VeinBlock;
import me.choco.veinminer.api.veinutils.VeinTool;
import me.choco.veinminer.utils.ConfigOption;
import me.choco.veinminer.utils.VeinMinerManager;

public class VeinMinerCmd implements CommandExecutor {
	
	private VeinMiner plugin;
	private VeinMinerManager manager;
	
	public VeinMinerCmd(VeinMiner plugin){
		this.plugin = plugin;
		this.manager = plugin.getVeinMinerManager();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (args.length >= 1){
			if (args[0].equalsIgnoreCase("reload")){
				if (!sender.hasPermission("veinminer.reload")){
					sendMessage(sender, "You don't have the sufficient permissions to run this command");
					return true;
				}
				
				plugin.reloadConfig();
				ConfigOption.loadConfigurationValues(plugin);
				manager.loadVeinableBlocks();
				sendMessage(sender, ChatColor.GREEN + "Configuration Successfully Reloaded");
			}
			
			else if (args[0].equalsIgnoreCase("version")){
				sender.sendMessage(ChatColor.GOLD + "--------------------------------------------");
				sender.sendMessage("");
				sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Version: " + ChatColor.RESET + ChatColor.GRAY  + plugin.getDescription().getVersion());
				sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Developer / Maintainer: " + ChatColor.RESET + ChatColor.GRAY + "2008Choco");
				sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Development Page: " + ChatColor.RESET + ChatColor.GRAY + "http://dev.bukkit.org/bukkit-plugins/vein-miner");
				sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Report Bugs To: " + ChatColor.RESET + ChatColor.GRAY + "http://dev.bukkit.org/bukkit-plugins/vein-miner/tickets");
				sender.sendMessage("");
				sender.sendMessage(ChatColor.GOLD + "--------------------------------------------");
			}
			
			else if (args[0].equalsIgnoreCase("toggle")){
				if (!(sender instanceof Player)){
					sendMessage(sender, "Cannot toggle VeinMiner for the console. You're not a player, silly!");
					return true;
				}
				
				Player player = (Player) sender;
				if (!canVeinMine(player)){
					sendMessage(player, "You can't toggle a feature you do not have access to, silly!");
					return true;
				}
				
				if (!player.hasPermission("veinminer.toggle")){
					sendMessage(player, "You don't have the sufficient permissions to run this command");
					return true;
				}
				
				// TOGGLE A SPECIFIC TOOL
				if (args.length >= 2){
					VeinTool tool = VeinTool.getByName(args[1]);
					if (tool == null){
						sendMessage(sender, "Invalid tool name, \"" + ChatColor.AQUA + args[1] + ChatColor.GRAY + "\"");
						return true;
					}

					manager.toggleVeinMiner(player, tool);
					boolean toggle = manager.hasVeinMinerDisabled(player, tool);
					sendMessage(player, "VeinMiner successfully toggled " + (toggle ? ChatColor.RED + "off" : ChatColor.GREEN + "on")
									+ ChatColor.GRAY + " for tool " + tool.name());
				}
				
				// TOGGLE ALL TOOLS
				else{
					boolean hasAllDisabled = true;
					for (VeinTool tool : VeinTool.values()){
						if (manager.hasVeinMinerEnabled(player, tool)){
							hasAllDisabled = false;
							break;
						}
					}
					
					for (VeinTool tool : VeinTool.values())
						manager.toggleVeinMiner(player, tool, hasAllDisabled);
					sendMessage(player, "VeinMiner successfully toggled " + (hasAllDisabled ? ChatColor.GREEN + "on" : ChatColor.RED + "off")
									+ ChatColor.GRAY + " for all tools!");
				}
			}
			
			else if (args[0].equalsIgnoreCase("blocklist")){
				if (args.length >= 2){
					VeinTool tool = VeinTool.getByName(args[1]);
					
					if (tool == null){
						sendMessage(sender, "Invalid tool name, \"" + ChatColor.AQUA + args[1] + ChatColor.GRAY + "\"");
						return true;
					}
					
					if (args.length >= 3){
						if (args[2].equalsIgnoreCase("add")){
							if (!sender.hasPermission("veinminer.blocklist.add")){
								sendMessage(sender, "You don't have the sufficient permissions to run this command");
								return true;
							}
							
							if (args.length >= 4){
								Material materialToAdd = Material.getMaterial(args[3].toUpperCase());
								if (materialToAdd == null){
									sendMessage(sender, "Block Id " + args[3] + " does not exist");
									return true;
								}
								
								if (!materialToAdd.isBlock()){
									sendMessage(sender, "An Item ID cannot be added to the blocklist");
									return true;
								}
								
								String matName = materialToAdd.name();
								List<String> blocklist = plugin.getConfig().getStringList("BlockList." + tool.getName());
								byte data = -1;
								if (args.length >= 5){
									try{
										data = Byte.parseByte(args[4]);
										if (data < -1){
											sendMessage(sender, "Data values below 0 are not possible");
											return true;
										}
									}catch(NumberFormatException e){
										sendMessage(sender, "Block data value must be a valid integer");
										return true;
									}
								}
								
								if (manager.isVeinable(tool, materialToAdd, data)){
									sendMessage(sender, "Block Id " + matName + " is already on the list");
									return true;
								}
								
								blocklist.add(matName + (data != -1 ? ";" + data : ""));
								plugin.getConfig().set("BlockList." + tool.getName(), blocklist);
								plugin.saveConfig(); plugin.reloadConfig();
								manager.registerVeinminableBlock(tool, new VeinBlock(materialToAdd, data));
								sendMessage(sender, "Block Id " + matName + (data != -1 ? " (Data: " + data + ")" : "") + " successfully added to the list");
							}else{
								 sendMessage(sender, "/veinminer blocklist add <id> [data]"); 
							}
						}
						
						else if (args[2].equalsIgnoreCase("remove")){
							if (!sender.hasPermission("veinminer.blocklist.remove")){
								sendMessage(sender, "You don't have the sufficient permissions to run this command");
								return true;
							}
							
							if (args.length >= 4){
								Material materialToRemove = Material.getMaterial(args[3].toUpperCase());
								if (materialToRemove == null){
									sendMessage(sender, "Block Id " + args[0] + " does not exist");
									return true;
								}
								
								if (!materialToRemove.isBlock()){
									sendMessage(sender, "An Item ID cannot be added to the blocklist");
									return true;
								}
								
								String matName = materialToRemove.name();
								List<String> blocklist = plugin.getConfig().getStringList("BlockList." + tool.getName());
								byte data = -1;
								if (args.length >= 5){
									try{
										data = Byte.parseByte(args[4]);
										if (data < -1){
											sendMessage(sender, "Data values below 0 are not possible");
											return true;
										}
									}catch(NumberFormatException e){
										sendMessage(sender, "Block data value must be a valid integer");
										return true;
									}
								}
								
								if (!manager.isVeinable(tool, materialToRemove, data)){
									sendMessage(sender, "Block Id " + matName + " is not on the list");
									return true;
								}
								
								blocklist.remove(matName + (data != -1 ? ";" + data : ""));
								plugin.getConfig().set("BlockList." + tool.getName(), blocklist);
								plugin.saveConfig(); plugin.reloadConfig();
								manager.unregisterVeinminableBlock(tool, materialToRemove, data);
								sendMessage(sender, "Block Id " + matName + (data != -1 ? " (Data: " + data + ")" : "") + " successfully removed from the list");
							}else{
								 sendMessage(sender, "/veinminer blocklist remove <id> [data]"); 
							}
						}
						
						else if (args[2].equalsIgnoreCase("list")){
							if (!sender.hasPermission("veinminer.blocklist.list." + tool.getName().toLowerCase())){
								sendMessage(sender, "You don't have the sufficient permissions to list this tool");
								return true;
							}
							
							Set<VeinBlock> blocklist = manager.getVeinminableBlocks(tool);
							sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "VeinMiner Blocklist (Tool = " + tool + "): ");
							for (VeinBlock block : blocklist){
								Material material = block.getMaterial();
								byte data = block.getData();
								sender.sendMessage(ChatColor.YELLOW + "[*] BlockID: " + material.name() + " - Data: " + (data == -1 ? "All" : data));
							}
						}
						
						else{
							sendMessage(sender, "/veinminer blocklist " + args[1] + "<add|remove|list|reset>"); 
						}
					}else{ sendMessage(sender, "/veinminer blocklist " + args[1] + " <add|remove|list|reset>"); }
				}else{ sendMessage(sender, "/veinminer blocklist <tool> <add|remove|list|reset>"); }
			}else{ sendMessage(sender, "/veinminer <reload|version|blocklist|toggle>"); }
		}else{ sendMessage(sender, "/veinminer <reload|version|blocklist|toggle>"); }
		return true;
	}
	
	private void sendMessage(CommandSender sender, String message){
		sender.sendMessage(ChatColor.BLUE + "VeinMiner" + ChatColor.DARK_BLUE + "> " + ChatColor.GRAY + message);
	}
	
	private boolean canVeinMine(Player player){
		if (player.hasPermission("veinminer.veinmine.*")) return true;
		for (VeinTool tool : VeinTool.values()){
			if (player.hasPermission("veinminer.veinmine." + tool.getName().toLowerCase())) 
				return true;
		}
		return false;
	}
}