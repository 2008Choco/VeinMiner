package wtf.choco.veinminer.util;

/**
 * General purpose constants used throughout VeinMiner.
 * <p>
 * Fields are intentionally left undocumented due to their self-explanatory nature.
 */
public final class VMConstants {

    // Configuration options
    public static final String CONFIG_METRICS_ENABLED = "MetricsEnabled";
    public static final String CONFIG_PERFORM_UPDATE_CHECKS = "PerformUpdateChecks";

    public static final String CONFIG_DEFAULT_ACTIVATION_STRATEGY = "DefaultActivationStrategy";
    public static final String CONFIG_DEFAULT_VEIN_MINING_PATTERN = "DefaultVeinMiningPattern";
    public static final String CONFIG_SORT_BLOCKLIST_ALPHABETICALLY = "SortBlocklistAlphabetically";
    public static final String CONFIG_COLLECT_ITEMS_AT_SOURCE = "CollectItemsAtSource";
    public static final String CONFIG_NERF_MCMMO = "NerfMcMMO";

    public static final String CONFIG_REPAIR_FRIENDLY_VEINMINER = "RepairFriendlyVeinminer";
    public static final String CONFIG_INCLUDE_EDGES = "IncludeEdges";
    public static final String CONFIG_MAX_VEIN_SIZE = "MaxVeinSize";
    public static final String CONFIG_COST = "Cost";

    public static final String CONFIG_DISABLED_GAME_MODES = "DisabledGameModes";
    public static final String CONFIG_DISABLED_WORLDS = "DisabledWorlds";

    public static final String CONFIG_HUNGER_HUNGER_MODIFIER = "Hunger.HungerModifier";
    public static final String CONFIG_HUNGER_MINIMUM_FOOD_LEVEL = "Hunger.MinimumFoodLevel";
    public static final String CONFIG_HUNGER_HUNGRY_MESSAGE = "Hunger.HungryMessage";

    public static final String CONFIG_CLIENT_ALLOW_CLIENT_ACTIVATION = "Client.AllowClientActivation";
    public static final String CONFIG_CLIENT_DISALLOWED_MESSAGE = "Client.DisallowedMessage";
    public static final String CONFIG_CLIENT_SUGGEST_CLIENT_MOD_PERIOD = "Client.SuggestClientModPeriod";
    public static final String CONFIG_CLIENT_SUGGESTION_MESSAGE = "Client.SuggestionMessage";

    public static final String CONFIG_STORAGE_TYPE = "Storage.Type";
    public static final String CONFIG_STORAGE_JSON_DIRECTORY = "Storage.JSON.Directory";
    public static final String CONFIG_STORAGE_MYSQL_HOST = "Storage.MySQL.Host";
    public static final String CONFIG_STORAGE_MYSQL_PORT = "Storage.MySQL.Port";
    public static final String CONFIG_STORAGE_MYSQL_USERNAME = "Storage.MySQL.Username";
    public static final String CONFIG_STORAGE_MYSQL_PASSWORD = "Storage.MySQL.Password";
    public static final String CONFIG_STORAGE_MYSQL_DATABASE = "Storage.MySQL.Database";
    public static final String CONFIG_STORAGE_MYSQL_TABLE_PREFIX = "Storage.MySQL.TablePrefix";

    public static final String CONFIG_ALIASES = "Aliases";


    // Permission nodes
    public static final String PERMISSION_COMMAND_RELOAD = "veinminer.command.reload";
    public static final String PERMISSION_COMMAND_BLOCKLIST = "veinminer.command.blocklist";
    public static final String PERMISSION_COMMAND_TOOLLIST = "veinminer.command.toollist";
    public static final String PERMISSION_COMMAND_TOGGLE = "veinminer.command.toggle";
    public static final String PERMISSION_COMMAND_MODE = "veinminer.command.mode";
    public static final String PERMISSION_COMMAND_PATTERN = "veinminer.command.pattern";

    public static final String PERMISSION_FREE_ECONOMY = "veinminer.free.economy";
    public static final String PERMISSION_FREE_HUNGER = "veinminer.free.hunger";

    // Dynamic permission nodes
    public static final String PERMISSION_DYNAMIC_VEINMINE = "veinminer.veinmine.%s";


    // Metadata keys
    public static final String METADATA_KEY_TO_BE_VEINMINED = "veinminer:to_be_veinmined";
    public static final String METADATA_KEY_VEINMINER_SOURCE = "veinminer:source";

    public static final String METADATA_KEY_VEINMINING = "veinminer:vein_mining";
    public static final String METADATA_KEY_VEIN_MINER_ACTIVE = "veinminer:vein_miner_active";

    private VMConstants() { }

}
