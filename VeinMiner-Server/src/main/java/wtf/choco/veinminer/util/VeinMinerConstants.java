package wtf.choco.veinminer.util;

import java.util.function.Function;

import wtf.choco.veinminer.tool.VeinMinerToolCategory;

/**
 * General purpose constants used throughout VeinMiner.
 * <p>
 * Fields are intentionally left undocumented due to their self-explanatory nature.
 */
public final class VeinMinerConstants {

    // Permission nodes
    public static final String PERMISSION_COMMAND_RELOAD = "veinminer.command.reload";
    public static final String PERMISSION_COMMAND_BLOCKLIST = "veinminer.command.blocklist";
    public static final String PERMISSION_COMMAND_TOOLLIST = "veinminer.command.toollist";
    public static final String PERMISSION_COMMAND_TOGGLE = "veinminer.command.toggle";
    public static final String PERMISSION_COMMAND_MODE = "veinminer.command.mode";
    public static final String PERMISSION_COMMAND_PATTERN = "veinminer.command.pattern";

    // Dynamic permission nodes
    public static final Function<VeinMinerToolCategory, String> PERMISSION_VEINMINE = category -> "veinminer.veinmine." + category.getId().toLowerCase();

    private VeinMinerConstants() { }

}
