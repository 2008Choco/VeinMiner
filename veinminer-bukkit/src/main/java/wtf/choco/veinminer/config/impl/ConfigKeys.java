package wtf.choco.veinminer.config.impl;

import org.jetbrains.annotations.NotNull;

final class ConfigKeys {

    // VeinMinerConfiguration
    static final String KEY_METRICS_ENABLED = "MetricsEnabled";
    static final String KEY_PERFORM_UPDATE_CHECKS = "PerformUpdateChecks";
    static final String KEY_DEFAULT_ACTIVATION_STRATEGY = "DefaultActivationStrategy";
    static final String KEY_DEFAULT_VEIN_MINING_PATTERN = "DefaultVeinMiningPattern";
    static final String KEY_COLLECT_ITEMS_AT_SOURCE = "CollectItemsAtSource";
    static final String KEY_NERF_MCMMO = "NerfMcMMO";
    static final String KEY_DISABLED_GAME_MODES = "DisabledGameModes";
    static final String KEY_HUNGER_HUNGER_MODIFIER = "Hunger.HungerModifier";
    static final String KEY_HUNGER_MINIMUM_FOOD_LEVEL = "Hunger.MinimumFoodLevel";
    static final String KEY_HUNGER_HUNGRY_MESSAGE = "Hunger.HungryMessage";
    static final String KEY_CLIENT_ALLOW_ACTIVATION_KEYBIND = "Client.AllowActivationKeybind";
    static final String KEY_CLIENT_ALLOW_PATTERN_SWITCHING_KEYBIND = "Client.AllowPatternSwitchingKeybind";
    static final String KEY_CLIENT_ALLOW_WIREFRAME_RENDERING = "Client.AllowWireframeRendering";
    static final String KEY_STORAGE_TYPE = "Storage.Type";
    static final String KEY_STORAGE_JSON_DIRECTORY = "Storage.JSON.Directory";
    static final String KEY_STORAGE_MYSQL_HOST = "Storage.MySQL.Host";
    static final String KEY_STORAGE_MYSQL_PORT = "Storage.MySQL.Port";
    static final String KEY_STORAGE_MYSQL_USERNAME = "Storage.MySQL.Username";
    static final String KEY_STORAGE_MYSQL_PASSWORD = "Storage.MySQL.Password";
    static final String KEY_STORAGE_MYSQL_DATABASE = "Storage.MySQL.Database";
    static final String KEY_STORAGE_MYSQL_TABLE_PREFIX = "Storage.MySQL.TablePrefix";
    static final String KEY_BLOCKLIST = "BlockList";
    static final String KEY_BLOCKLIST_ALL = KEY_BLOCKLIST + ".All";
    static final String KEY_ALIASES = "Aliases";

    static String keyBlockList(@NotNull String categoryId) {
        return KEY_BLOCKLIST + categoryId;
    }

    // ToolCategoryConfiguration
    static final String KEY_PRIORITY = "Priority";
    static final String KEY_NBT = "NBT";
    static final String KEY_ITEMS = "Items";

    // VeinMiningConfiguration
    static final String KEY_REPAIR_FRIENDLY = "RepairFriendly";
    static final String KEY_MAX_VEIN_SIZE = "MaxVeinSize";
    static final String KEY_COST = "Cost";
    static final String KEY_DISABLED_WORLDS = "DisabledWorlds";

}
