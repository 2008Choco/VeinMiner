package me.choco.veinminer.utils;

import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;

import me.choco.veinminer.VeinMiner;

public class ConfigOption {
	
	public static boolean METRICS_ENABLED;
	public static String ACTIVATION_MODE;
	public static List<String> DISABLED_WORLDS;
	
	public static int PICKAXE_MAX_VEIN_SIZE;
	public static boolean PICKAXE_USES_DURABILITY;
	public static int AXE_MAX_VEIN_SIZE;
	public static boolean AXE_USES_DURABILITY;
	public static int SHOVEL_MAX_VEIN_SIZE;
	public static boolean SHOVEL_USES_DURABILITY;
	public static int HOE_MAX_VEIN_SIZE;
	public static boolean HOE_USES_DURABILITY;
	public static int SHEARS_MAX_VEIN_SIZE;
	public static boolean SHEARS_USES_DURABILITY;
	
	public static void loadConfigurationValues(VeinMiner plugin){
		FileConfiguration config = plugin.getConfig();
		
		METRICS_ENABLED = config.getBoolean("MetricsEnabled", true);
		ACTIVATION_MODE = config.getString("ActivationMode", "SNEAK");
		DISABLED_WORLDS = config.getStringList("DisabledWorlds");
		
		PICKAXE_MAX_VEIN_SIZE = config.getInt("Tools.Pickaxe.MaxVeinSize", 64);
		PICKAXE_USES_DURABILITY = config.getBoolean("Tools.Pickaxe.UsesDurability", true);
		AXE_MAX_VEIN_SIZE = config.getInt("Tools.Axe.MaxVeinSize", 64);
		PICKAXE_USES_DURABILITY = config.getBoolean("Tools.Axe.UsesDurability", true);
		SHOVEL_MAX_VEIN_SIZE = config.getInt("Tools.Shovel.MaxVeinSize", 64);
		PICKAXE_USES_DURABILITY = config.getBoolean("Tools.Shovel.UsesDurability", true);
		HOE_MAX_VEIN_SIZE = config.getInt("Tools.Hoe.MaxVeinSize", 64);
		PICKAXE_USES_DURABILITY = config.getBoolean("Tools.Hoe.UsesDurability", true);
		SHEARS_MAX_VEIN_SIZE = config.getInt("Tools.Shears.MaxVeinSize", 64);
		PICKAXE_USES_DURABILITY = config.getBoolean("Tools.Shears.UsesDurability", true);
		
		
	}
}