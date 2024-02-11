package wtf.choco.veinminer.config.migrator;

import org.bukkit.plugin.Plugin;

/**
 * A {@link PostMigrationStep} implementation that will save a plugin's config.yml.
 */
public final class PostMigrationStepSaveDefault implements PostMigrationStep {

    private final Plugin plugin;

    /**
     * Construct a new {@link PostMigrationStepSaveDefault}.
     *
     * @param plugin the plugin whose config.yml to save
     */
    PostMigrationStepSaveDefault(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        this.plugin.saveConfig();
    }

}
