package wtf.choco.veinminer.tool;

import java.util.function.Predicate;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.data.AlgorithmConfig;

/**
 * Represents a template for a {@link ToolCategory}'s vein mining tool. The {@link ItemStack}
 * used to vein mine must match this template (such that {@link #matches(ItemStack)} is true).
 *
 * @author Parker Hawke - 2008Choco
 */
public interface ToolTemplate {

    /**
     * Check whether or not the provided item matches this template.
     *
     * @param item the item to check
     *
     * @return true if matches, false otherwise
     */
    public boolean matches(@Nullable ItemStack item);

    /**
     * Get the algorithm config for this tool template. This template should have precedence
     * over the its category algorithm config as well as the global algorithm config.
     *
     * @return the algorithm config
     */
    @NotNull
    public AlgorithmConfig getConfig();

    /**
     * Get the category from which this tool template resides.
     *
     * @return the belonging tool category
     */
    @NotNull
    public ToolCategory getCategory();

    /**
     * Get this template as a {@literal Predicate<ItemStack>}
     *
     * @return the template predicate
     */
    @NotNull
    public default Predicate<ItemStack> asPredicate() {
        return this::matches;
    }

}
