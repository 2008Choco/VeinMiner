package wtf.choco.veinminer.config.migrator;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMinerPlugin;

public final class MigrationStepAliases implements MigrationStep {

    private static final String KEY_ALIASES = "Aliases";
    private static final String UNNAMED_ALIAS_PATTERN = "unnamed_alias_%d";

    @Override
    public String getDescription() {
        return "Convert Aliases in config.yml from a list of strings to a section with named string lists, each a block entry";
    }

    @Override
    public boolean shouldApply(@NotNull VeinMinerPlugin plugin) {
        return plugin.getConfig().isList(KEY_ALIASES);
    }

    @Override
    public void apply(@NotNull VeinMinerPlugin plugin) {
        FileConfiguration config = plugin.getConfig();

        List<String> existingAliases = config.getStringList(KEY_ALIASES);
        ConfigurationSection aliasesSection = config.createSection(KEY_ALIASES);

        int id = 1;
        for (String alias : existingAliases) {
            String aliasName = String.format(UNNAMED_ALIAS_PATTERN, id++);
            String[] entries = alias.split(";");
            aliasesSection.set(aliasName, List.of(entries));
        }

        config.set(KEY_ALIASES, aliasesSection);
    }

    @NotNull
    @Override
    public List<PostMigrationStep> getPostMigrationSteps(@NotNull VeinMinerPlugin plugin) {
        return List.of(PostMigrationStep.saveDefault(plugin));
    }

}
