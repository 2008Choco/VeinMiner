package wtf.choco.veinminer.config.migrator;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMinerPlugin;

/**
 * A step that must occur during a configuration migration.
 */
public interface MigrationStep {

    /**
     * Get this step's description to be shown in the console log when executed.
     *
     * @return the description
     */
    public String getDescription();

    /**
     * Check whether or not this migration step should even run. If this method returns
     * false, this step is skipped.
     *
     * @param plugin the plugin instance
     *
     * @return true if should apply, false if it should skip
     */
    public boolean shouldApply(@NotNull VeinMinerPlugin plugin);

    /**
     * Apply this migration step.
     *
     * @param plugin the plugin instance
     */
    public void apply(@NotNull VeinMinerPlugin plugin);

    /**
     * Get a list of {@link PostMigrationStep PostMigrationSteps} to invoke after all
     * migration steps have been run.
     *
     * @param plugin the plugin instance
     *
     * @return the post-migration steps
     */
    @NotNull
    public default List<PostMigrationStep> getPostMigrationSteps(@NotNull VeinMinerPlugin plugin) {
        return Collections.emptyList();
    }

    /**
     * Get the "Move Block Lists To categories.yml" migration step instance.
     *
     * @return the migration step instance
     */
    @NotNull
    public static MigrationStep blockListsToCategoriesFile() {
        return MigrationStepBlockListsToCategoriesFile.INSTANCE;
    }

}
