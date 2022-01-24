package wtf.choco.veinminer.util;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class ConfigWrapper {

    private final JavaPlugin plugin;
    private final String rawPath;

    private final File file;
    private FileConfiguration config;

    public ConfigWrapper(@NotNull JavaPlugin plugin, @NotNull File directory, String path) {
        this(plugin, directory.getPath().concat(path));
    }

    public ConfigWrapper(@NotNull JavaPlugin plugin, @NotNull String path) {
        Preconditions.checkArgument(plugin != null, "Cannot provide null plugin");
        Preconditions.checkArgument(!StringUtils.isEmpty(path), "File path must not be null");

        this.plugin = plugin;
        this.rawPath = path;
        this.file = new File(plugin.getDataFolder(), path);
        if (!file.exists()) { // Doing this only to remove the unnecessary warning from Bukkit when saving an existing file -,-
            plugin.saveResource(path, false);
        }

        this.config = YamlConfiguration.loadConfiguration(file);
    }

    @NotNull
    public FileConfiguration asRawConfig() {
        return config;
    }

    public void saveExceptionally() throws IOException {
        this.config.save(file);
    }

    public void save() {
        try {
            this.saveExceptionally();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        this.config = YamlConfiguration.loadConfiguration(file);

        // Loading defaults if necessary
        final InputStream defaultConfigStream = plugin.getResource(rawPath);
        if (defaultConfigStream == null) {
            return;
        }

        this.config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defaultConfigStream, Charsets.UTF_8)));
    }

}
