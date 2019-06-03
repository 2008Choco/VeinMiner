package wtf.choco.veinminer.tool.template;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.tool.ToolCategory;

/**
 * A utility class that acts as a medium for checking the validity of templates against an
 * ItemStack passed to the {@link #isValid(ItemStack, ToolCategory)} method.
 *
 * @author Parker Hawke - 2008Choco
 */
public final class TemplateValidator {

    public static final Set<ToolCategory> TEMPLATABLE_CATEGORIES = Sets.immutableEnumSet(ToolCategory.AXE, ToolCategory.HOE, ToolCategory.PICKAXE, ToolCategory.SHEARS, ToolCategory.SHOVEL);

    private ToolTemplate globalTemplate;
    private TemplatePrecedence precedence;

    private final Map<ToolCategory, ToolTemplate> categoryTemplates;

    private TemplateValidator(@NotNull TemplatePrecedence precedence, @Nullable ToolTemplate globalTemplate, @Nullable Map<ToolCategory, ToolTemplate> categoryTemplates) {
        this.precedence = precedence;
        this.globalTemplate = globalTemplate;
        this.categoryTemplates = (categoryTemplates != null) ? new EnumMap<>(categoryTemplates) : new EnumMap<>(ToolCategory.class);
    }

    /**
     * Start the construction of a TemplateValidator with a template for a specific category.
     *
     * @param category the category for which to set a template
     * @param template the template to set
     *
     * @return the validator builder to further define templates and parameters
     */
    @NotNull
    public static ValidatorBuilder withTemplate(@NotNull ToolCategory category, @NotNull ToolTemplate template) {
        Preconditions.checkArgument(category != null, "Cannot define a template for a null category");
        Preconditions.checkArgument(template != null, "Cannot set a null template whilst building the validator");

        return new ValidatorBuilder(category, template);
    }

    /**
     * Start the construction of a TemplateValidator with a global template.
     *
     * @param template the global template to set
     *
     * @return the validator builder to further define templates and parameters
     */
    @NotNull
    public static ValidatorBuilder withGlobalTemplate(@NotNull ToolTemplate template) {
        Preconditions.checkArgument(template != null, "Cannot set a null global template whilst building the validator");
        return new ValidatorBuilder(template);
    }

    /**
     * Start the construction of a TemplateValidator with a template precedence other than
     * the default ({@link TemplatePrecedence#CATEGORY_SPECIFIC}).
     *
     * @param precedence the precedence to set
     *
     * @return the validator builder to further define templates and parameters
     */
    @NotNull
    public static ValidatorBuilder withPrecedence(@NotNull TemplatePrecedence precedence) {
        Preconditions.checkArgument(precedence != null, "Precedence must not be null");
        return new ValidatorBuilder(precedence);
    }

    /**
     * Create an empty TemplateValidator instance with the default template precedence
     * ({@link TemplatePrecedence#CATEGORY_SPECIFIC}).
     *
     * @return the empty template validator
     */
    @NotNull
    public static TemplateValidator empty() {
        return new TemplateValidator(TemplatePrecedence.CATEGORY_SPECIFIC, null, null);
    }

    /**
     * Set the precedence to be used for this validator.
     *
     * @param precedence the precedence to set
     */
    public void setPrecedence(@NotNull TemplatePrecedence precedence) {
        this.precedence = precedence;
    }

    /**
     * Get the precedence used for this validator.
     *
     * @return the template precedence
     */
    @NotNull
    public TemplatePrecedence getPrecedence() {
        return precedence;
    }

    /**
     * Set the global template to be used when validating items.
     *
     * @param template the global template to set
     */
    public void setGlobalTemplate(@Nullable ToolTemplate template) {
        this.globalTemplate = template;
    }

    /**
     * Get this validator's global template.
     *
     * @return the global template
     */
    @Nullable
    public ToolTemplate getGlobalTemplate() {
        return globalTemplate;
    }

    /**
     * Check whether or not this validator has a global template defined.
     *
     * @return true if defined, false otherwise
     */
    public boolean hasGlobalTemplate() {
        return globalTemplate != null;
    }

    /**
     * Set a template for a specific category.
     *
     * @param category the category for which to set a template
     * @param template the template to set
     */
    public void setTemplate(@NotNull ToolCategory category, @Nullable ToolTemplate template) {
        if (template != null) {
            this.categoryTemplates.put(category, template);
        } else {
            this.categoryTemplates.remove(category);
        }
    }

    /**
     * Get the template for the specified category.
     *
     * @param category the category whose template to get
     *
     * @return the template. null if none defined for the specified category
     */
    @Nullable
    public ToolTemplate getTemplate(@NotNull ToolCategory category) {
        return categoryTemplates.get(category);
    }

    /**
     * Check whether or not the specified category has a defined template.
     *
     * @param category the category to check
     *
     * @return true if a template is defined, false otherwise
     */
    public boolean hasTemplate(@NotNull ToolCategory category) {
        return categoryTemplates.containsKey(category);
    }

    /**
     * Clear all category-specific templates and the global template from this validator.
     */
    public void clear() {
        this.categoryTemplates.clear();
        this.globalTemplate = null;
    }

    /**
     * Check whether or not the provided item and category are valid according to this
     * validator's template criteria and precedence.
     *
     * @param item the item to check
     * @param category the category to check
     *
     * @return true if valid, false otherwise
     */
    public boolean isValid(@Nullable ItemStack item, @NotNull ToolCategory category) {
        if (item == null) { // If null, true if HAND, false if another category
            return category == ToolCategory.HAND;
        }

        if (precedence == TemplatePrecedence.GLOBAL && globalTemplate != null) {
            return globalTemplate.matches(item);
        } else if (precedence == TemplatePrecedence.CATEGORY_SPECIFIC_GLOBAL_DEFAULT) {
            ToolTemplate template = categoryTemplates.get(category);
            if (template != null) {
                return template.matches(item);
            } else if (globalTemplate != null) {
                return globalTemplate.matches(item);
            }
        } else if (precedence == TemplatePrecedence.CATEGORY_SPECIFIC) {
            ToolTemplate template = categoryTemplates.get(category);
            if (template != null) {
                return template.matches(item);
            }
        }

        return category.contains(item.getType());
    }

    public static final class ValidatorBuilder {

        private ToolTemplate globalTemplate;
        private TemplatePrecedence precedence = TemplatePrecedence.CATEGORY_SPECIFIC;
        private Map<ToolCategory, ToolTemplate> categoryTemplates = null;

        private ValidatorBuilder(@NotNull ToolCategory category, @Nullable ToolTemplate template) {
            this.categoryTemplates = new EnumMap<>(ToolCategory.class);
            this.categoryTemplates.put(category, template);
        }

        private ValidatorBuilder(@Nullable ToolTemplate globalTemplate) {
            this.globalTemplate = globalTemplate;
        }

        private ValidatorBuilder(@NotNull TemplatePrecedence precedence) {
            this.precedence = precedence;
        }

        /**
         * Set a template for a specific category.
         *
         * @param category the category for which to set a template
         * @param template the template to set
         *
         * @return this instance. Allows for chained method invocations
         */
        @NotNull
        public ValidatorBuilder template(@NotNull ToolCategory category, @NotNull ToolTemplate template) {
            Preconditions.checkArgument(category != null, "Cannot define a template for a null category");
            Preconditions.checkArgument(template != null, "Cannot set a null template whilst building the validator");

            if (categoryTemplates == null) {
                this.categoryTemplates = new EnumMap<>(ToolCategory.class);
            }

            this.categoryTemplates.put(category, template);
            return this;
        }

        /**
         * Set the global template to be used when validating items.
         *
         * @param template the global template to set
         *
         * @return this instance. Allows for chained method invocations
         */
        @NotNull
        public ValidatorBuilder globalTemplate(@NotNull ToolTemplate template) {
            Preconditions.checkArgument(template != null, "Cannot set a null global template whilst building the validator");

            this.globalTemplate = template;
            return this;
        }

        /**
         * Set the precedence to be used.
         *
         * @param precedence the precedence to set
         *
         * @return this instance. Allows for chained method invocations
         */
        @NotNull
        public ValidatorBuilder precedence(@NotNull TemplatePrecedence precedence) {
            Preconditions.checkArgument(precedence != null, "Precedence must not be null");

            this.precedence = precedence;
            return this;
        }

        /**
         * Build and return the TemplateValidator.
         *
         * @return the created validator
         */
        @NotNull
        public TemplateValidator build() {
            return new TemplateValidator(precedence, globalTemplate, categoryTemplates);
        }

    }

}
