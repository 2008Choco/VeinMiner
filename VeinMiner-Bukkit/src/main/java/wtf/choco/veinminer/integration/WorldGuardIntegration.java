package wtf.choco.veinminer.integration;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import wtf.choco.veinminer.VeinMiner;

public final class WorldGuardIntegration {

    private static final StateFlag FLAG_VEINMINER = new StateFlag("veinminer", true);

    private static boolean initialized = false;

    private WorldGuardIntegration() { }

    public static void init(VeinMiner plugin) {
        registerFlag(plugin, WorldGuard.getInstance().getFlagRegistry(), FLAG_VEINMINER);
        initialized = true;
    }

    public static boolean queryFlagVeinMiner(Block block, Player player) {
        return initialized && testFlag(block, player, FLAG_VEINMINER);
    }

    private static boolean testFlag(Block block, Player player, StateFlag flag) {
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = regionContainer.get(localPlayer.getWorld());
        ApplicableRegionSet regionSet = regionManager.getApplicableRegions(BlockVector3.at(block.getX(), block.getY(), block.getZ()));

        return regionSet.testState(localPlayer, flag);
    }

    private static void registerFlag(JavaPlugin plugin, FlagRegistry flagRegistry, StateFlag flag) {
        try {
            flagRegistry.register(flag);
        } catch (FlagConflictException e) {
            plugin.getLogger().warning("A flag with the name \"" + flag.getName() + "\" already exists and could not be registered.");
            e.printStackTrace();
        }
    }

}
