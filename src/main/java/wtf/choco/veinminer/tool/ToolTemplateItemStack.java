package wtf.choco.veinminer.tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.google.common.base.Preconditions;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.data.AlgorithmConfig;
import wtf.choco.veinminer.utils.ItemValidator;

/**
 * A {@link ToolTemplate} validated against an {@link ItemStack} and its {@link ItemMeta}
 *
 * @author Parker Hawke - 2008Choco
 */
public class ToolTemplateItemStack implements ToolTemplate {

    private final ToolCategory category;
    private final Material type;
    private final String name;
    private final List<String> lore;
    private final AlgorithmConfig config;

    /**
     * Construct a new ToolTemplate with a specific type, name and lore.
     *
     * @param category the category to which this template belongs
     * @param type the type for which to check
     * @param name the name for which to check (null if none)
     * @param lore the lore for which to check (null if none)
     */
    public ToolTemplateItemStack(@NotNull ToolCategory category, @NotNull Material type, @Nullable String name, @Nullable List<String> lore) {
        Preconditions.checkArgument(category != null, "Cannot provide a null category");
        Preconditions.checkArgument(!ItemValidator.isEmpty(type), "The specified type must be an item. Blocks, technical blocks or air are not permitted");

        this.category = category;
        this.type = type;
        this.name = (!StringUtils.isWhitespace(name)) ? name : null;
        this.lore = (lore != null && !lore.isEmpty()) ? new ArrayList<>(lore) : null;
        this.config = new AlgorithmConfig(category.getConfig());
    }

    /**
     * Construct a new ToolTemplate based upon an existing ItemStack
     *
     * @param category the category to which this template belongs
     * @param item the item from which to create a template
     */
    public ToolTemplateItemStack(@NotNull ToolCategory category, @NotNull ItemStack item) {
        Preconditions.checkArgument(category != null, "Cannot provide a null category");
        Preconditions.checkArgument(item != null, "Item must not be null");

        this.category = category;
        this.type = item.getType();

        ItemMeta meta = item.getItemMeta();
        this.name = (meta != null && meta.hasDisplayName()) ? meta.getDisplayName() : null;
        this.lore = (meta != null && meta.hasLore()) ? new ArrayList<>(meta.getLore()) : null;
        this.config = new AlgorithmConfig(category.getConfig());
    }

    /**
     * Get the specific type defined by this template. If {@link Material#AIR}, no specific type is
     * defined.
     *
     * @return the specific type
     */
    @NotNull
    public Material getType() {
        return type;
    }

    /**
     * Get the item name defined by this template. Chat colours may or may not be translated in the
     * result of this value depending on the implementation. No standard is defined.
     *
     * @return the name of the template. null if none
     */
    @Nullable
    public String getName() {
        return name;
    }

    /**
     * Get the item lore defined by this template. Chat colours may or may not be translated in the
     * result of this value depending on the implementation. No standard is defined.
     *
     * @return the lore of the template. null if none
     */
    @Nullable
    public List<String> getLore() {
        return (lore != null) ? Collections.unmodifiableList(lore) : null;
    }

    @Override
    public boolean matches(@Nullable ItemStack item) {
        if (item == null || item.getType() != type) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (name != null && (meta == null || !name.equals(meta.getDisplayName()))) {
            return false;
        }

        return lore == null || (meta != null && meta.hasLore() && lore.equals(meta.getLore()));
    }

    @Override
    public AlgorithmConfig getConfig() {
        return config;
    }

    @Override
    public ToolCategory getCategory() {
        return category;
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, type, name, lore);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof ToolTemplateItemStack)) return false;

        ToolTemplateItemStack other = (ToolTemplateItemStack) obj;
        return type == other.type && Objects.equals(name, other.name) && Objects.equals(lore, other.lore);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(type.getKey().toString());

        boolean data = (name != null || lore != null);
        if (data) {
            result.append(" (");
        }

        if (name != null) {
            result.append("Name: \"" + ChatColor.stripColor(name) + "\"");
        }

        if (lore != null) {
            if (name != null) {
                result.append(", ");
            }

            result.append("Lore: " + ChatColor.stripColor(lore.toString()));
        }

        if (data) {
            result.append(')');
        }

        return result.toString();
    }

}
