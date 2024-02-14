package wtf.choco.veinminer.util;

import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.tool.VeinMinerToolCategory;

/**
 * General purpose constants used throughout VeinMiner.
 * <p>
 * Fields are intentionally left undocumented due to their self-explanatory nature.
 */
public final class VMConstants {

    // Permission nodes
    public static final String PERMISSION_FREE_ECONOMY = "veinminer.free.economy";
    public static final String PERMISSION_FREE_HUNGER = "veinminer.free.hunger";

    public static final String PERMISSION_CLIENT_ACTIVATION = "veinminer.client.activation";
    public static final String PERMISSION_CLIENT_PATTERNS = "veinminer.client.patterns";
    public static final String PERMISSION_CLIENT_WIREFRAME = "veinminer.client.wireframe";

    public static final String PERMISSION_COMMAND_RELOAD = "veinminer.command.reload";
    public static final String PERMISSION_COMMAND_BLOCKLIST = "veinminer.command.blocklist";
    public static final String PERMISSION_COMMAND_TOOLLIST = "veinminer.command.toollist";
    public static final String PERMISSION_COMMAND_GIVETOOL = "veinminer.command.givetool";
    public static final String PERMISSION_COMMAND_TOGGLE = "veinminer.command.toggle";
    public static final String PERMISSION_COMMAND_MODE = "veinminer.command.mode";
    public static final String PERMISSION_COMMAND_PATTERN = "veinminer.command.pattern";
    public static final String PERMISSION_COMMAND_IMPORT = "veinminer.command.import";

    // Dynamic permission nodes
    public static final Function<VeinMinerToolCategory, String> PERMISSION_VEINMINE = category -> "veinminer.veinmine." + category.getId().toLowerCase();


    // Metadata keys
    public static final String METADATA_KEY_TO_BE_VEINMINED = "veinminer:to_be_veinmined";
    public static final String METADATA_KEY_VEINMINER_SOURCE = "veinminer:source";
    public static final String METADATA_KEY_VEINMINER_EXPERIENCE = "veinminer:experience";

    public static final String METADATA_KEY_VEINMINING = "veinminer:vein_mining";
    public static final String METADATA_KEY_VEIN_MINER_ACTIVE = "veinminer:vein_miner_active";

    private static org.bukkit.NamespacedKey key;

    private VMConstants() { }

    /**
     * Get the {@link org.bukkit.NamespacedKey NamespacedKey} of the NBT key for items.
     *
     * @return the NBT key
     */
    @NotNull
    public static org.bukkit.NamespacedKey getVeinMinerNBTKey() {
        if (key == null) {
            key = VeinMinerPlugin.key("veinminer");
        }

        return key;
    }

}
