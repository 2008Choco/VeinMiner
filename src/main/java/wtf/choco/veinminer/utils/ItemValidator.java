package wtf.choco.veinminer.utils;

import java.util.EnumSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.tool.ToolCategory;

/**
 * A utility class that acts as a medium for checking the validity of items and materials.
 *
 * @author Parker Hawke - 2008Choco
 */
public final class ItemValidator {

    private static final Set<Material> AIR = EnumSet.of(Material.AIR, Material.CAVE_AIR, Material.VOID_AIR);

    private ItemValidator() { }

    /**
     * Check whether or not the provided item is valid according to the provided category's
     * available item templates.
     *
     * @param item the item to check
     * @param category the category to check
     *
     * @return true if valid, false otherwise
     */
    public static boolean isValid(@Nullable ItemStack item, @NotNull ToolCategory category) {
        if (item == null || item.getType() == Material.AIR) { // If null, true if HAND, false if another category
            return category == ToolCategory.HAND;
        }

        return category.getTools().stream().anyMatch(t -> t.matches(item));
    }

    /**
     * Check whether or not the provided item is empty. An item is considered empty if it
     * is either null or one of the three types of air.
     *
     * @param item the item to check
     *
     * @return true if empty or null, false otherwise
     */
    public static boolean isEmpty(@Nullable ItemStack item) {
        return item == null || AIR.contains(item.getType());
    }

    /**
     * Check whether or not the provided material is empty. A material is considered empty
     * if it is one of the three types of air.
     *
     * @param material the material to check
     *
     * @return true if empty or null, false otherwise
     */
    public static boolean isEmpty(@Nullable Material material) {
        return AIR.contains(material);
    }

}
