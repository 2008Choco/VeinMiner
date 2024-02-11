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

        try {
            /*
             * setComments() was added in 1.18.1, but it's not big enough of a feature for me to bump past 1.17 support
             * TODO: Remove try-catch when a version after 1.18 is required
             */
            rawCategoriesConfig.setComments("Hand", List.of("Does not support an \"Items\" list, but does support all other options"));
            rawCategoriesConfig.setComments("All", List.of(
                "Does not support any configurable values other than \"BlockList\"",
                "Applies this list of blocks to all other categories, to avoid repetition"
            ));
        } catch (Exception | Error e) { /* ignore */ }

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
