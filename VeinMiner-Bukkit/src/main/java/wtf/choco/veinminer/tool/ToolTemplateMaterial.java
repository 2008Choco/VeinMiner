package wtf.choco.veinminer.tool;

import com.google.common.base.Preconditions;

import java.util.Objects;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.data.AlgorithmConfig;

/**
 * A {@link ToolTemplate} validated against a {@link Material} with any meta.
 *
 * @author Parker Hawke - 2008Choco
 */
public final class ToolTemplateMaterial implements ToolTemplate, Comparable<ToolTemplateMaterial> {

    private final ToolCategory category;
    private final Material material;
    private final AlgorithmConfig config;

    /**
     * Construct a new ToolTemplate with a specific type.
     *
     * @param category the category to which this template belongs
     * @param material the material for which to check
     * @param configuration the algorithm configuration for this template
     */
    public ToolTemplateMaterial(@NotNull ToolCategory category, @NotNull Material material, @NotNull AlgorithmConfig configuration) {
        Preconditions.checkArgument(category != null, "Cannot provide a null category");
        Preconditions.checkArgument(material != null, "Material must not be null");
        Preconditions.checkArgument(configuration != null, "configuration must not be null");

        this.category = category;
        this.material = material;
        this.config = configuration;
    }

    /**
     * Construct a new ToolTemplate with a specific type.
     *
     * @param category the category to which this template belongs
     * @param material the material for which to check
     */
    public ToolTemplateMaterial(@NotNull ToolCategory category, @NotNull Material material) {
        this(category, material, category.getConfig().clone());
    }

    /**
     * Get this template's material.
     *
     * @return the material
     */
    public Material getMaterial() {
        return material;
    }

    @Override
    public boolean matches(@Nullable ItemStack item) {
        return item != null && item.getType() == material;
    }

    @NotNull
    @Override
    public AlgorithmConfig getConfig() {
        return config;
    }

    @NotNull
    @Override
    public ToolCategory getCategory() {
        return category;
    }

    @Override
    public int compareTo(ToolTemplateMaterial o) {
        return material.compareTo(o.material);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, material);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof ToolTemplateMaterial)) {
            return false;
        }

        ToolTemplateMaterial other = (ToolTemplateMaterial) obj;
        return category == other.category && material == other.material;
    }

    @Override
    public String toString() {
        return material.getKey().toString();
    }

}
