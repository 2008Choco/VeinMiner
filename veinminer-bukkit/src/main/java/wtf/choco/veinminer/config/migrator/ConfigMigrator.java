package wtf.choco.veinminer.config.migrator;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMinerPlugin;

/**
 * An object capable of migrating configuration files in a sequence of steps.
 */
public final class ConfigMigrator {

    private boolean migrated = false;

    private final List<MigrationStep> steps = new ArrayList<>();
    private final VeinMinerPlugin plugin;

    /**
     * Construct a new {@link ConfigMigrator}.
     *
     * @param plugin the plugin instance
     */
    public ConfigMigrator(@NotNull VeinMinerPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Add a new {@link MigrationStep} to this migrator.
     *
     * @param step the migration step to add
     */
    public void addStep(@NotNull MigrationStep step) {
        this.steps.add(step);
    }

    /**
     * Invoke the migration.
     * <p>
     * This can only be invoked once per instance. After this method has been invoked, an exception
     * will be thrown on subsequent invocations.
     *
     * @return the amount of migration steps that occurred
     */
    public int migrate() {
        if (migrated) {
            throw new IllegalStateException("This ConfigMigrator has already been used, cannot migrate again");
        }

        this.migrated = true;

        List<PostMigrationStep> postMigrationSteps = new ArrayList<>();

        int migrations = 0;
        for (MigrationStep step : steps) {
            if (!step.shouldApply(plugin)) {
                continue;
            }

            this.plugin.getLogger().info("Migrating configuration... Step: \"" + step.getDescription() + "\"");
            step.apply(plugin);

            for (PostMigrationStep postMigrationStep : step.getPostMigrationSteps(plugin)) {
                if (postMigrationSteps.contains(postMigrationStep)) {
                    continue;
                }

                postMigrationSteps.add(postMigrationStep);
            }

            migrations++;
        }

        for (PostMigrationStep step : postMigrationSteps) {
            step.run();
        }

        return migrations;
    }

}
