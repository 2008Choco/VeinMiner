package wtf.choco.veinminer.integration;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.player.VeinMinerPlayer;
import wtf.choco.veinminer.tool.VeinMinerToolCategory;
import wtf.choco.veinminer.util.AttributeUtil;
import wtf.choco.veinminer.util.StringUtil;
import wtf.choco.veinminer.util.VMConstants;

/**
 * A {@link PlaceholderExpansion PlaceholderAPI expansion} for VeinMiner's placeholders.
 */
public final class PlaceholderExpansionVeinMiner extends PlaceholderExpansion {

    private static final List<String> PLACEHOLDERS = List.of(
            // Requires a player
            "veinminer_enabled",
            "veinminer_enabled_<category>",
            "veinminer_active",
            "veinminer_vein_mining",
            "veinminer_using_client_mod",
            "veinminer_pattern",
            "veinminer_activation_strategy",
            "veinminer_vein_mineable",
            "veinminer_vein_mineable_<category>",
            "veinminer_category",
            "veinminer_category_formatted",
            // Evaluates differently whether or not a player is supplied
            "veinminer_cost",
            "veinminer_cost_<category>",
            "veinminer_max_vein_size",
            // Evaluates the same whether or not a player is supplied
            "veinminer_max_vein_size_<category>"
    );

    private static final Map<Integer, NumberFormat> NUMBER_FORMATS = new HashMap<>();

    private final VeinMinerPlugin plugin;

    /**
     * Construct a new {@link PlaceholderExpansionVeinMiner}.
     *
     * @param plugin the plugin instance
     */
    public PlaceholderExpansionVeinMiner(@NotNull VeinMinerPlugin plugin) {
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
        return plugin.getDescription().getVersion();
    }

    @NotNull
    @Override
    public List<String> getPlaceholders() {
        return PLACEHOLDERS;
    }

    @Override
    public String onPlaceholderRequest(@Nullable Player player, @NotNull String identifier) {
        // START: Evaluates the same regardless of whether or not a player is supplied
        if (identifier.startsWith("max_vein_size_")) {
            return withCategoryParameter(identifier, 2, this::maxVeinSize);
        }
        // END

        // START: Evaluates without the need for a supplied player
        if (player == null) {
            if (identifier.equals("cost")) {
                return cost(null, null);
            }

            else if (identifier.startsWith("cost_")) {
                return withCategoryParameter(identifier, 0, this::cost);
            }

            else if (identifier.equals("max_vein_size")) {
                return maxVeinSize(null);
            }

            return null;
        }
        // END

        VeinMinerPlayer veinMinerPlayer = plugin.getPlayerManager().get(player);
        if (veinMinerPlayer == null) {
            return null;
        }

        // START: Evaluates with the need for a supplied player
        if (identifier.equals("enabled")) {
            return String.valueOf(veinMinerPlayer.isVeinMinerEnabled());
        }

        else if (identifier.startsWith("enabled_")) {
            return withCategoryParameter(identifier, 0, category -> String.valueOf(veinMinerPlayer.isVeinMinerEnabled(category)));
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

        else if (identifier.equals("pattern")) {
            return veinMinerPlayer.getVeinMiningPattern().getKey().toString();
        }

        else if (identifier.equals("activation_strategy")) {
            return veinMinerPlayer.getActivationStrategy().getFriendlyName();
        }

        else if (identifier.equals("vein_mineable")) {
            Block targetBlock = getRayTracedTargetBlock(player);
            VeinMinerToolCategory category = plugin.getToolCategoryRegistry().get(player.getInventory().getItemInMainHand());
            if (targetBlock == null || category == null) {
                return "false";
            }

            return String.valueOf(plugin.getVeinMinerManager().isVeinMineable(targetBlock.getBlockData(), category));
        }

        else if (identifier.startsWith("vein_mineable_")) {
            return withCategoryParameter(identifier, 1, category -> {
                Block targetBlock = getRayTracedTargetBlock(player);
                if (targetBlock == null) {
                    return "false";
                }

                return String.valueOf(plugin.getVeinMinerManager().isVeinMineable(targetBlock.getBlockData(), category));
            });
        }

        else if (identifier.equals("category")) {
            VeinMinerToolCategory category = plugin.getToolCategoryRegistry().get(player.getInventory().getItemInMainHand());
            return (category != null) ? category.getId() : "None";
        }

        else if (identifier.equals("cost")) {
            VeinMinerToolCategory category = plugin.getToolCategoryRegistry().get(player.getInventory().getItemInMainHand());
            return cost(player, category);
        }

        else if (identifier.startsWith("cost_")) {
            return withCategoryParameter(identifier, 0, category -> cost(player, category));
        }

        else if (identifier.equals("max_vein_size")) {
            VeinMinerToolCategory category = plugin.getToolCategoryRegistry().get(player.getInventory().getItemInMainHand());
            return (category != null) ? maxVeinSize(category) : "0";
        }

        return null;
    }

    /**
     * Tries to parse a category id from an input string using the remaining characters after the {@code offset}
     * instance index of a {@code '_'} character, and passes the VeinMinerToolCategory instance of the registered
     * category with the id to the provided Function, or returns the fallback value supplied in the Supplier if
     * there is no category with the parsed id. If there is no specified id after the {@code '_'} character, then
     * {@code null} will be returned instead. For example:
     * <pre>
     * withCategoryParameter("identifier_category_id", 0, category {@literal ->} category.getId(), null); // Gets category with id "category_id"
     * withCategoryParameter("identifier_two_category_id, 1, category {@literal ->} category.getId(), null); // Gets the category with the id "category_id"
     * withCategoryParameter("identifier_INVALID_CATEGORY_ID", 0, category {@literal ->} category.getId(), () {@literal ->} "unknown category"); // returns "unknown category"
     * withCategoryParameter("identifier_", 0, {@literal ->} category.getId(), null); // returns null
     * withCategoryParameter("identifier_example, 5, {@literal ->} category.getId(), null); // returns null
     * </pre>
     *
     * @param identifier the input identifier
     * @param offset the instance of the {@code '_'} where to start parsing a category id
     * @param extractor the extractor function to use after a category has been found
     * @param noCategoryFallback the fallback supplier to use if a category does not exist with the parsed id.
     * If {@code null}, {@code null} will be returned if the category does not exist
     *
     * @return the result of the {@code extractor} function for an existing category, the result of the
     * {@code noCategoryFallback} supplier if the category did not exist, or null if a category id could
     * not be parsed for one reason or another
     */
    private String withCategoryParameter(@NotNull String identifier, int offset, @NotNull Function<VeinMinerToolCategory, String> extractor, @Nullable Supplier<String> noCategoryFallback) {
        String categoryId = StringUtil.substringFromOccurrence(identifier, '_', offset);
        if (categoryId == null) {
            return null;
        }

        VeinMinerToolCategory category = plugin.getToolCategoryRegistry().get(categoryId);
        if (category == null) {
            return (noCategoryFallback != null) ? noCategoryFallback.get() : null;
        }

        return extractor.apply(category);
    }

    private String withCategoryParameter(@NotNull String identifier, int offset, @NotNull Function<VeinMinerToolCategory, String> extractor) {
        return withCategoryParameter(identifier, offset, extractor, null);
    }

    private String cost(@Nullable Player player, @Nullable VeinMinerToolCategory category) {
        double cost = (category != null ? category.getConfiguration().getCost() : plugin.getConfiguration().getCost());
        boolean free = player != null && player.hasPermission(VMConstants.PERMISSION_FREE_ECONOMY);

        int fractionalDigits = plugin.getEconomy().getFractionalDigits();
        return getNumberFormat(fractionalDigits).format(free ? 0.0 : cost);
    }

    private static NumberFormat getNumberFormat(int fractionalDigits) {
        if (fractionalDigits < 0) {
            fractionalDigits = 0;
        }

        return NUMBER_FORMATS.computeIfAbsent(fractionalDigits, digits -> (digits == 0) ? new DecimalFormat("0") : new DecimalFormat("0." + "0".repeat(digits)));
    }

    private String cost(@Nullable VeinMinerToolCategory category) {
        return cost(null, category);
    }

    private String maxVeinSize(@Nullable VeinMinerToolCategory category) {
        return String.valueOf(category != null ? category.getConfiguration().getMaxVeinSize() : plugin.getConfiguration().getMaxVeinSize());
    }

    private Block getRayTracedTargetBlock(Player player) {
        RayTraceResult result = player.rayTraceBlocks(AttributeUtil.getReachDistance(player));
        if (result == null) {
            return null;
        }

        return result.getHitBlock();
    }

}
