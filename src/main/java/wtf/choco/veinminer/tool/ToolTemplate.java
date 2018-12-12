package wtf.choco.veinminer.tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import com.google.common.base.Preconditions;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Represents a template for a {@link ToolCategory}'s vein mining tool. The {@link ItemStack}
 * used to vein mine must match this template (such that {@link #matches(ItemStack)} is true).
 *
 * @author Parker Hawke - 2008Choco
 */
public class ToolTemplate {

	private final ToolCategory category;
	private final Material specificType;
	private final String name;
	private final List<String> lore;

	/**
	 * Construct a new ToolTemplate with no specific type.
	 *
	 * @param category the category to which this template belongs. Must not be null
	 * @param name the name for which to check (null if none)
	 * @param lore the lore for which to check (null if none)
	 */
	public ToolTemplate(ToolCategory category, String name, List<String> lore) {
		Preconditions.checkArgument(category != null, "Category must not be null");

		this.category = category;
		this.name = name;
		this.lore = (lore != null) ? new ArrayList<>(lore) : null;
		this.specificType = Material.AIR;
	}

	/**
	 * Construct a new ToolTemplate with a specific type. The type provided must be one of the default
	 * types defined by a {@link ToolCategory} (i.e. such that {@link ToolCategory#fromMaterial(Material)}
	 * is not equal to {@link ToolCategory#HAND}
	 *
	 * @param specificType the specific type for which to check. Must be encapsulated by a category. null
	 * is also acceptable for hands, but may prove to be useless.
	 * @param name the name for which to check (null if none)
	 * @param lore the lore for which to check (null if none)
	 */
	public ToolTemplate(Material specificType, String name, List<String> lore) {
		this.category = ToolCategory.fromMaterial(specificType);
		if (category == ToolCategory.HAND && specificType != null) {
			throw new IllegalArgumentException("The provided material (" + specificType.getKey() + ") does not belong to any existing tool category");
		}

		this.specificType = specificType;
		this.name = name;
		this.lore = (lore != null) ? new ArrayList<>(lore) : null;
	}

	/**
	 * Get the category to which this template belongs.
	 *
	 * @return the associated category
	 */
	public ToolCategory getCategory() {
		return category;
	}

	/**
	 * Get the specific type defined by this template. If {@link Material#AIR}, no specific type is
	 * defined and all types defined by {@link #getCategory()} are valid.
	 *
	 * @return the specific type
	 */
	public Material getSpecificType() {
		return specificType;
	}

	/**
	 * Check whether or not this template defines a specific type.
	 *
	 * @return true if a specific type is defined, false otherwise
	 */
	public boolean hasSpecificType() {
		return specificType != Material.AIR;
	}

	/**
	 * Get the item name defined by this template. Chat colours may or may not be translated in the
	 * result of this value depending on the implementation. No standard is defined.
	 *
	 * @return the name of the template. null if none
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the item lore defined by this template. Chat colours may or may not be translated in the
	 * result of this value depending on the implementation. No standard is defined.
	 *
	 * @return the lore of the template. null if none
	 */
	public List<String> getLore() {
		return (lore != null) ? Collections.unmodifiableList(lore) : null;
	}

	/**
	 * Check whether or not the provided item matches this template.
	 *
	 * @param item the item to check
	 *
	 * @return true if matches, false otherwise
	 */
	public boolean matches(ItemStack item) {
		if (category != ToolCategory.HAND && item == null) {
			return false;
		}

		if (!hasSpecificType() && !category.contains(item.getType())) {
			return false;
		} else if (hasSpecificType() && specificType != item.getType()) {
			return false;
		}

		ItemMeta meta = item.getItemMeta();
		if (name != null && (meta == null || !meta.hasDisplayName() || !meta.getDisplayName().equals(name))) {
			return false;
		}

		if (lore != null && (meta == null || !meta.hasLore() || !meta.getLore().equals(lore))) {
			return false;
		}

		return true;
	}

	/**
	 * Get this template as a {@link Predicate}.
	 *
	 * @return this template as an ItemStack predicate
	 */
	public Predicate<ItemStack> asPredicate() {
		return this::matches;
	}

	@Override
	public int hashCode() {
		return Objects.hash(category, specificType, name, lore);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof ToolTemplate)) return false;

		ToolTemplate other = (ToolTemplate) obj;
		return category == other.category && specificType == other.specificType
			&& Objects.equals(name, other.name) && Objects.equals(lore, other.lore);
	}

	/**
	 * Get an empty ToolTemplate for the specified category. An empty template implies that all items will
	 * match such that its type is defined by the specified category.
	 *
	 * @param category the category for which to create an empty template
	 *
	 * @return the empty template
	 */
	public static ToolTemplate empty(ToolCategory category) {
		return new ToolTemplate(category, null, null);
	}

}