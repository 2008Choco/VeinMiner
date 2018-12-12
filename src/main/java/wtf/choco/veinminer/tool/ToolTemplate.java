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

public class ToolTemplate {

	private final ToolCategory category;
	private final Material specificType;
	private final String name;
	private final List<String> lore;

	public ToolTemplate(ToolCategory category, String name, List<String> lore) {
		Preconditions.checkArgument(category != null, "Category must not be null");

		this.category = category;
		this.name = name;
		this.lore = (lore != null) ? new ArrayList<>(lore) : null;
		this.specificType = Material.AIR;
	}

	public ToolTemplate(Material specificType, String name, List<String> lore) {
		this.category = ToolCategory.fromMaterial(specificType);
		if (category == ToolCategory.HAND && specificType != null) {
			throw new IllegalArgumentException("The provided material (" + specificType.getKey() + ") does not belong to any existing tool category");
		}

		this.specificType = specificType;
		this.name = name;
		this.lore = (lore != null) ? new ArrayList<>(lore) : null;
	}

	public ToolCategory getCategory() {
		return category;
	}

	public Material getSpecificType() {
		return specificType;
	}

	public boolean hasSpecificType() {
		return specificType != Material.AIR;
	}

	public String getName() {
		return name;
	}

	public List<String> getLore() {
		return Collections.unmodifiableList(lore);
	}

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

	public static ToolTemplate empty(ToolCategory category) {
		return new ToolTemplate(category, null, null);
	}

}