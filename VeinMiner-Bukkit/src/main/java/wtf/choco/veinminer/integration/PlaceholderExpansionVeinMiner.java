package wtf.choco.veinminer.integration;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.network.VeinMinerPlayer;
import wtf.choco.veinminer.tool.VeinMinerToolCategory;

/**
 * A {@link PlaceholderExpansion} for VeinMiner's placeholders.
 * <p>
 * Supported placeholders:
 * <ul>
 *   <li>%veinminer_enabled%: Whether or not at least one vein miner category is enabled
 *   <li>%veinminer_enabled_{@literal <tool>}%: Whether or not the vein miner category is enabled
 *   <li>%veinminer_active%: Whether or not vein miner is active and ready to use
 * </ul>
 */
public final class PlaceholderExpansionVeinMiner extends PlaceholderExpansion {

    private final VeinMinerPlugin plugin;

    /**
     * Construct a new {@link PlaceholderExpansionVeinMiner}.
     *
     * @param plugin the plugin instance
     */
    public PlaceholderExpansionVeinMiner(VeinMinerPlugin plugin) {
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
            return String.valueOf(veinMinerPlayer.isVeinMinerActive());
        }

        else if (identifier.equals("vein_mining")) {
            return String.valueOf(veinMinerPlayer.isVeinMining());
        }

        else if (identifier.equals("using_client_mod")) {
            return String.valueOf(veinMinerPlayer.isUsingClientMod());
        }

        else if (identifier.equals("selected_pattern")) {
            return veinMinerPlayer.getVeinMiningPattern().getKey().toString();
        }

        else if (identifier.equals("activation_strategy")) {
            return veinMinerPlayer.getActivationStrategy().getFriendlyName();
        }

        return null;
    }

}
