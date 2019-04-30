package wtf.choco.veinminer.tool.template;

import wtf.choco.veinminer.tool.ToolCategory;

/**
 * Represents the precedence (or priority) of templates to be checked when defined in the
 * configuration file.
 *
 * @author Parker Hawke - 2008Choco
 */
public enum TemplatePrecedence {

    /**
     * The global template should always be used. If no global template is defined, and instead any
     * tool type covered under the identified {@link ToolCategory} is considered valid.
     */
    GLOBAL,

    /**
     * The category-specific template should always be used. If no category-specific template is
     * defined, any tool type covered under the identified {@link ToolCategory} is considered valid.
     */
    CATEGORY_SPECIFIC,

    /**
     * The category-specific template is used, but if not defined, the global template is used
     * instead. If no category-specific or global template is defined, any tool type covered under
     * the identified {@link ToolCategory} is considered valid.
     */
    CATEGORY_SPECIFIC_GLOBAL_DEFAULT;

}