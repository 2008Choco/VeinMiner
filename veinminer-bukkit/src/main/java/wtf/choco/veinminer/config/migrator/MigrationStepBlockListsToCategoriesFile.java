package wtf.choco.veinminer.config.migrator;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.config.ConfigWrapper;

public final class MigrationStepBlockListsToCategoriesFile implements MigrationStep {

    private static final String KEY_BLOCK_LIST = "BlockList";

    static final MigrationStep INSTANCE = new MigrationStepBlockListsToCategoriesFile();

    MigrationStepBlockListsToCategoriesFile() { }

    @Override
    public String getDescription() {
        return "Move Block Lists To categories.yml";
    }

    @Override
    public boolean shouldApply(@NotNull VeinMinerPlugin plugin) {
        return plugin.getConfig().isConfigurationSection(KEY_BLOCK_LIST);
    }

    @Override
    public void apply(@NotNull VeinMinerPlugin plugin) {
        FileConfiguration config = plugin.getConfig();

        ConfigurationSection blockListSection = config.getConfigurationSection(KEY_BLOCK_LIST);
        if (blockListSection == null) {
            throw new IllegalStateException("Missing BlockList section even though it exists. How?");
        }

        ConfigWrapper categoriesConfig = plugin.getCategoriesConfig();
        FileConfiguration rawCategoriesConfig = categoriesConfig.asRawConfig();

        for (String categoryId : blockListSection.getKeys(false)) {
            rawCategoriesConfig.set(categoryId + "." + KEY_BLOCK_LIST, config.getStringList(KEY_BLOCK_LIST + "." + categoryId));
        }

        config.set(KEY_BLOCK_LIST, "Moved to categories.yml");
    }

    @NotNull
    @Override
    public List<PostMigrationStep> getPostMigrationSteps(@NotNull VeinMinerPlugin plugin) {
        return List.of(
                PostMigrationStep.save(plugin.getCategoriesConfig()),
                PostMigrationStep.saveDefault(plugin)
        );
    }

}
