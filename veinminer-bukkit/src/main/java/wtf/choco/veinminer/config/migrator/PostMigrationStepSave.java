package wtf.choco.veinminer.config.migrator;

import wtf.choco.veinminer.config.ConfigWrapper;

/**
 * A {@link PostMigrationStep} implementation that will save a {@link ConfigWrapper}.
 */
public final class PostMigrationStepSave implements PostMigrationStep {

    private final ConfigWrapper configuration;

    /**
     * Construct a new {@link PostMigrationStep}.
     *
     * @param configuration the configuration to save
     */
    PostMigrationStepSave(ConfigWrapper configuration) {
        this.configuration = configuration;
    }

    @Override
    public void run() {
        this.configuration.save();
    }

    @Override
    public int hashCode() {
        return configuration.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof PostMigrationStepSave other && configuration.equals(other.configuration));
    }

}
