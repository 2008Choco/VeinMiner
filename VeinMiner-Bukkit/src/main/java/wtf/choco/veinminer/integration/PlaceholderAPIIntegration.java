package wtf.choco.veinminer.integration;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.api.ActivationStrategy;
import wtf.choco.veinminer.data.PlayerPreferences;
import wtf.choco.veinminer.tool.ToolCategory;

public class PlaceholderAPIIntegration extends PlaceholderExpansion {

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @NotNull String getAuthor() {
        return "2008Choco";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "veinminer";
    }

    @Override
    public @NotNull String getVersion() {
        return VeinMiner.getPlugin().getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        final PlayerPreferences playerPreferences = PlayerPreferences.get(player);

        if (identifier.equals("enabled")) {
            return String.valueOf(playerPreferences.isVeinMinerEnabled());
        }

        else if (identifier.startsWith("enabled_category_")) {
            ToolCategory category = ToolCategory.get(identifier.split("_")[2]);
            if (category == null) {
                return null;
            }

            return String.valueOf(playerPreferences.isVeinMinerEnabled(category));
        }

        else if (identifier.equals("active")) {
            PlayerPreferences playerData = PlayerPreferences.get(player);
            ActivationStrategy activation = playerData.getActivationStrategy();

            return String.valueOf(activation.isValid(player));
        }

        return null;
    }
}
