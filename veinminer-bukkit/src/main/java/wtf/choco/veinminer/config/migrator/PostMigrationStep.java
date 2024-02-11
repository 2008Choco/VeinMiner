package wtf.choco.veinminer.config.migrator;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.config.ConfigWrapper;

/**
 * A step to run after a successful {@link MigrationStep}.
 */
public interface PostMigrationStep extends Runnable {

    /**
     * {@inheritDoc}
     * <p>
     * <strong>MUST BE OVERRIDDEN BY IMPLEMENTATIONS!</strong>
     */
    @Override
    public int hashCode();

    /**
     * {@inheritDoc}
     * <p>
     * <strong>MUST BE OVERRIDDEN BY IMPLEMENTATIONS!</strong>
     */
    @Override
    public boolean equals(Object other);

    /**
     * Create a new "save" post migration step for {@link ConfigWrapper} instances.
     * <p>
     * After migration, the provided ConfigWrapper will be saved.
     *
     * @param config the config to save
     *
     * @return the post migration step instance
     */
    @NotNull
    public static PostMigrationStep save(@NotNull ConfigWrapper config) {
        return new PostMigrationStepSave(config);
    }

    /**
     * Create a new "save" post migration step for the standard config.yml for the given {@link Plugin}.
     * <p>
     * After migration, the provided plugin's config.yml will be saved.
     *
     * @param plugin the plugin instance whose config to save
     *
     * @return the post migration step instance
     */
    @NotNull
    public static PostMigrationStep saveDefault(@NotNull Plugin plugin) {
        return new PostMigrationStepSaveDefault(plugin);
    }

}
