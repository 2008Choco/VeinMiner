package wtf.choco.veinminer.language;

import org.jetbrains.annotations.ApiStatus.Internal;

/**
 * A class holding a set of constants representing the keys recognized by VeinMiner's language file.
 * <p>
 * Constants may be removed from this class at any point without warning or deprecation. This is not
 * an API safe class!
 */
@Internal
public final class LanguageKeys {

    public static final String COMMAND_INSUFFICIENT_PERMISSIONS = "command.insufficient_permissions";
    public static final String COMMAND_INVALID_KEY = "command.invalid_key";
    public static final String COMMAND_UNKNOWN_CATEGORY = "command.unknown_category";
    public static final String COMMAND_UNKNOWN_ITEM = "command.unknown_item";
    public static final String COMMAND_VEINMINER_RELOAD_SUCCESS = "command.veinminer.reload.success";
    public static final String COMMAND_VEINMINER_VERSION_BORDER = "command.veinminer.version.border";
    public static final String COMMAND_VEINMINER_VERSION_VERSION = "command.veinminer.version.version";
    public static final String COMMAND_VEINMINER_VERSION_VERSION_ALERT = "command.veinminer.version.version_alert";
    public static final String COMMAND_VEINMINER_VERSION_VERSION_UPDATE_AVAILABLE = "command.veinminer.version.version.update_available";
    public static final String COMMAND_VEINMINER_VERSION_VERSION_DEV_BUILD = "command.veinminer.version.version.dev_build";
    public static final String COMMAND_VEINMINER_VERSION_VERSION_FAILED = "command.veinminer.version.version.failed";
    public static final String COMMAND_VEINMINER_VERSION_DEVELOPER = "command.veinminer.version.developer";
    public static final String COMMAND_VEINMINER_VERSION_WEBSITE = "command.veinminer.version.website";
    public static final String COMMAND_VEINMINER_VERSION_SOURCE_CODE = "command.veinminer.version.source_code";
    public static final String COMMAND_VEINMINER_TOGGLE_CONSOLE = "command.veinminer.toggle.console";
    public static final String COMMAND_VEINMINER_TOGGLE_SUCCESS_ALL_ON = "command.veinminer.toggle.success.all_on";
    public static final String COMMAND_VEINMINER_TOGGLE_SUCCESS_CATEGORY_ON = "command.veinminer.toggle.success.category_on";
    public static final String COMMAND_VEINMINER_TOGGLE_SUCCESS_ALL_OFF = "command.veinminer.toggle.success.all_off";
    public static final String COMMAND_VEINMINER_TOGGLE_SUCCESS_CATEGORY_OFF = "command.veinminer.toggle.success.category_off";
    public static final String COMMAND_VEINMINER_MODE_CONSOLE = "command.veinminer.mode.console";
    public static final String COMMAND_VEINMINER_MODE_INVALID = "command.veinminer.mode.invalid";
    public static final String COMMAND_VEINMINER_MODE_NO_CLIENT_MOD = "command.veinminer.mode.no_client_mod";
    public static final String COMMAND_VEINMINER_MODE_CLIENT_MOD_INFO = "command.veinminer.mode.client_mod_info";
    public static final String COMMAND_VEINMINER_MODE_CLIENT_MOD_SUPPORTS = "command.veinminer.mode.client_mod_supports";
    public static final String COMMAND_VEINMINER_MODE_SUCCESS = "command.veinminer.mode.success";
    public static final String COMMAND_VEINMINER_PATTERN_CONSOLE = "command.veinminer.pattern.console";
    public static final String COMMAND_VEINMINER_PATTERN_UNKNOWN_PATTERN = "command.veinminer.pattern.unknown_pattern";
    public static final String COMMAND_VEINMINER_PATTERN_NO_PERMISSION = "command.veinminer.pattern.no_permission";
    public static final String COMMAND_VEINMINER_PATTERN_SUCCESS = "command.veinminer.pattern.success";
    public static final String COMMAND_VEINMINER_IMPORT_NON_IMPORTABLE = "command.veinminer.import.non_importable";
    public static final String COMMAND_VEINMINER_IMPORT_WARNING = "command.veinminer.import.warning";
    public static final String COMMAND_VEINMINER_IMPORT_DESTRUCTIVE = "command.veinminer.import.destructive";
    public static final String COMMAND_VEINMINER_IMPORT_DESCRIPTION = "command.veinminer.import.description";
    public static final String COMMAND_VEINMINER_IMPORT_DO_ONCE = "command.veinminer.import.do_once";
    public static final String COMMAND_VEINMINER_IMPORT_CONFIRM = "command.veinminer.import.confirm";
    public static final String COMMAND_VEINMINER_IMPORT_SEARCHING = "command.veinminer.import.searching";
    public static final String COMMAND_VEINMINER_IMPORT_NO_DATA = "command.veinminer.import.no_data";
    public static final String COMMAND_VEINMINER_IMPORT_FOUND = "command.veinminer.import.found";
    public static final String COMMAND_VEINMINER_IMPORT_DONE = "command.veinminer.import.done";
    public static final String COMMAND_VEINMINER_IMPORT_IMPORTING = "command.veinminer.import.importing";
    public static final String COMMAND_VEINMINER_IMPORT_FAIL_PLAYER = "command.veinminer.import.fail.player";
    public static final String COMMAND_VEINMINER_IMPORT_FAIL_UNKNOWN = "command.veinminer.import.fail.unknown";
    public static final String COMMAND_VEINMINER_IMPORT_SUCCESS = "command.veinminer.import.success";
    public static final String COMMAND_VEINMINER_IMPORT_SUCCESS_FAILURES = "command.veinminer.import.success.failures";
    public static final String COMMAND_VEINMINER_GIVETOOL_CONSOLE = "command.veinminer.givetool.console";
    public static final String COMMAND_VEINMINER_GIVETOOL_NO_ITEMS = "command.veinminer.givetool.no_items";
    public static final String COMMAND_VEINMINER_GIVETOOL_UNSUPPORTED_ITEM = "command.veinminer.givetool.unsupported_item";
    public static final String COMMAND_VEINMINER_GIVETOOL_INVENTORY_FULL = "command.veinminer.givetool.inventory_full";
    public static final String COMMAND_VEINMINER_GIVETOOL_SUCCESS = "command.veinminer.givetool.success";
    public static final String COMMAND_BLOCKLIST_ADD_EXISTS = "command.blocklist.add.exists";
    public static final String COMMAND_BLOCKLIST_ADD_SUCCESS = "command.blocklist.add.success";
    public static final String COMMAND_BLOCKLIST_REMOVE_MISSING = "command.blocklist.remove.missing";
    public static final String COMMAND_BLOCKLIST_REMOVE_SUCCESS = "command.blocklist.remove.success";
    public static final String COMMAND_BLOCKLIST_LIST_EMPTY = "command.blocklist.list.empty";
    public static final String COMMAND_BLOCKLIST_LIST_HEADER = "command.blocklist.list.header";
    public static final String COMMAND_BLOCKLIST_LIST_ENTRY = "command.blocklist.list.entry";
    public static final String COMMAND_TOOLLIST_ADD_HAND = "command.toollist.add.hand";
    public static final String COMMAND_TOOLLIST_ADD_EXISTS = "command.toollist.add.exists";
    public static final String COMMAND_TOOLLIST_ADD_SUCCESS = "command.toollist.add.success";
    public static final String COMMAND_TOOLLIST_REMOVE_HAND = "command.toollist.remove.hand";
    public static final String COMMAND_TOOLLIST_REMOVE_TOO_FEW_ITEMS = "command.toollist.remove.too_few_items";
    public static final String COMMAND_TOOLLIST_REMOVE_MISSING = "command.toollist.remove.missing";
    public static final String COMMAND_TOOLLIST_REMOVE_SUCCESS = "command.toollist.remove.success";
    public static final String COMMAND_TOOLLIST_LIST_EMPTY = "command.toollist.list.empty";
    public static final String COMMAND_TOOLLIST_LIST_HEADER = "command.toollist.list.header";
    public static final String COMMAND_TOOLLIST_LIST_ENTRY = "command.toollist.list.entry";
    public static final String PLACEHOLDER_NONE = "placeholder.none";
    public static final String VEINMINER_WORLDGUARD = "veinminer.worldguard";
    public static final String VEINMINER_INSUFFICIENT_FUNDS = "veinminer.insufficient_funds";
    public static final String VEINMINER_HUNGRY = "veinminer.hungry";
    public static final String VEINMINER_CLIENT_VERSION_MISMATCH_OUT_OF_DATE = "veinminer.client.version_mismatch.out_of_date";
    public static final String VEINMINER_CLIENT_VERSION_MISMATCH_TOO_NEW = "veinminer.client.version_mismatch.too_new";

    private LanguageKeys() { }

}
