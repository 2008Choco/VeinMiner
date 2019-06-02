package wtf.choco.veinminer.tool;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.VeinMiner;

/**
 * Tool categories recognised by VeinMiner and it's code. Tool materials are limited
 * to those listed in the enumeration.
 */
public enum ToolCategory {

    /**
     * Represents a pickaxe of various materials. This includes:
     * <ul>
     *   <li>Wooden Pickaxe
     *   <li>Stone Pickaxe
     *   <li>Golden Pickaxe
     *   <li>Iron Pickaxe
     *   <li>Diamond Pickaxe
     * </ul>
     */
    PICKAXE("Pickaxe", Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.GOLDEN_PICKAXE, Material.IRON_PICKAXE, Material.DIAMOND_PICKAXE),

    /**
     * Represents an axe of various materials. This includes:
     * <ul>
     *   <li>Wooden Axe
     *   <li>Stone Axe
     *   <li>Golden Axe
     *   <li>Iron Axe
     *   <li>Diamond Axe
     * </ul>
     */
    AXE("Axe", Material.WOODEN_AXE, Material.STONE_AXE, Material.GOLDEN_AXE, Material.IRON_AXE, Material.DIAMOND_AXE),

    /**
     * Represents a shovel of various materials. This includes:
     * <ul>
     *   <li>Wooden Shovel
     *   <li>Stone Shovel
     *   <li>Golden Shovel
     *   <li>Iron Shovel
     *   <li>Diamond Shovel
     * </ul>
     */
    SHOVEL("Shovel", Material.WOODEN_SHOVEL, Material.STONE_SHOVEL, Material.GOLDEN_SHOVEL, Material.IRON_SHOVEL, Material.DIAMOND_SHOVEL),

    /**
     * Represents a hoe of various materials. This includes:
     * <ul>
     *   <li>Wooden Hoe
     *   <li>Stone Hoe
     *   <li>Golden Hoe
     *   <li>Iron Hoe
     *   <li>Diamond Hoe
     * </ul>
     */
    HOE("Hoe", Material.WOODEN_HOE, Material.STONE_HOE, Material.GOLDEN_HOE, Material.IRON_HOE, Material.DIAMOND_HOE),

    /**
     * Represents shears
     */
    SHEARS("Shears", Material.SHEARS),

    /**
     * Represent's a player's hands; i.e. no tool at all
     */
    HAND("Hand", false);


    private static final VeinMiner plugin = VeinMiner.getPlugin();

    private final String name;
    private final Set<Material> materials;
    private final boolean canHaveToolTemplate;

    ToolCategory(@NotNull String name, boolean canHaveToolTemplate, @NotNull Material... materials) {
        this.name = name;
        this.canHaveToolTemplate = canHaveToolTemplate;
        this.materials = Collections.unmodifiableSet((materials.length != 0) ? EnumSet.of(materials[0], materials) : EnumSet.noneOf(Material.class));
    }

    ToolCategory(@NotNull String name, @NotNull Material... materials) {
        this(name, true, materials);
    }

    /**
     * Get the name for this tool used in the configuration file.
     *
     * @return the name
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Check whether or not this tool category may specify a tool template.
     *
     * @return true if a template is possible, false otherwise
     */
    public boolean canHaveToolTemplate() {
        return canHaveToolTemplate;
    }

    /**
     * Get all Materials associated with this tool category.
     *
     * @return all associated tool materials
     */
    @NotNull
    public Set<Material> getMaterials() {
        return materials; // Immutable
    }

    /**
     * Check whether or not the provided material is considered a part of this category.
     *
     * @param material the material to check
     *
     * @return true if contained in this category, false otherwise
     */
    public boolean contains(@NotNull Material material) {
        return materials.contains(material);
    }

    /**
     * Get the maximum vein size this tool category is capable of breaking. This option is specified
     * in and directly retrieved from the configuration file.
     *
     * @return the maximum vein size. Defaults to 64 if not explicitly set
     */
    public int getMaxVeinSize() {
        return plugin.getConfig().getInt("Tools." + name + ".MaxVeinSize", 64);
    }

    /**
     * Get a tool category based on its name used in the configuration file. null if no VeinTool with
     * the given name exists.
     *
     * @param name the name of the category. Case insensitive
     *
     * @return the ToolCategory with the given name. null if none
     */
    @Nullable
    public static ToolCategory getByName(@NotNull String name) {
        for (ToolCategory category : values())
            if (category.getName().equalsIgnoreCase(name)) return category;
        return null;
    }

    /**
     * Get the tool category associated with the specified material. If none exist, {@link #HAND} is
     * returned.
     *
     * @param material the material for which to search
     *
     * @return the ToolCategory associated with the specified material. {@link #HAND} if none
     */
    @NotNull
    public static ToolCategory fromMaterial(@NotNull Material material) {
        for (ToolCategory category : values()) {
            if (category.contains(material)) {
                return category;
            }
        }

        return HAND;
    }

    /**
     * Get the tool category associated with the specific item. If none exist or the item is null,
     * {@link #HAND} is returned.
     *
     * @param item the item for which to search
     *
     * @return the ToolCategory associated with the specified item. {@link #HAND} if none
     */
    @NotNull
    public static ToolCategory fromItemStack(@NotNull ItemStack item) {
        return (item != null) ? fromMaterial(item.getType()) : HAND;
    }

}
