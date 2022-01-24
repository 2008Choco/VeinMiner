package wtf.choco.veinminer.integration;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.network.VeinMinerPlayer;
import wtf.choco.veinminer.tool.VeinMinerToolCategory;

public class PlaceholderAPIIntegration extends PlaceholderExpansion {

    private final VeinMinerPlugin plugin;

    public PlaceholderAPIIntegration(VeinMinerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @NotNull
    @Override
    public String getAuthor() {
        return "Choco";
    }

    @NotNull
    @Override
    public String getIdentifier() {
        return "veinminer";
    }

    @NotNull
    @Override
    public String getVersion() {
        return VeinMinerPlugin.getInstance().getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(@Nullable Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        VeinMinerPlayer veinMinerPlayer = plugin.getPlayerManager().get(player);

        if (identifier.equals("enabled")) {
            return String.valueOf(veinMinerPlayer.isVeinMinerEnabled());
        }

        else if (identifier.startsWith("enabled_category_")) {
            VeinMinerToolCategory category = plugin.getToolCategoryRegistry().get(identifier.split("_")[2]);
            if (category == null) {
                return null;
            }

            return String.valueOf(veinMinerPlayer.isVeinMinerEnabled(category));
        }

        else if (identifier.equals("active")) {
            return String.valueOf(veinMinerPlayer.getActivationStrategy().isActive(player));
        }

        return null;
    }

}
