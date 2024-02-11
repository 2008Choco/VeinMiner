package wtf.choco.veinminer.config;

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

/**
 * A simple wrapper to more easily work with {@link YamlConfiguration} instances.
 */
public final class ConfigWrapper {

    private final JavaPlugin plugin;
    private final String rawPath;

    private final File file;
    private FileConfiguration config;

    /**
     * Construct a new {@link ConfigWrapper}.
     *
     * @param plugin the plugin instance
     * @param directory the directory at which the config is located
     * @param name the name of the config file
     */
    public ConfigWrapper(@NotNull JavaPlugin plugin, @NotNull File directory, @NotNull String name) {
        this(plugin, directory.getPath().concat(name));
    }

    /**
     * Construct a new {@link ConfigWrapper}.
     *
     * @param plugin the plugin instance
     * @param path the path at which the config is located
     */
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

    /**
     * Get this {@link ConfigWrapper} as a raw {@link FileConfiguration} instance.
     *
     * @return the raw config
     */
    @NotNull
    public FileConfiguration asRawConfig() {
        return config;
    }

    /**
     * Save this {@link ConfigWrapper} and throw an {@link IOException} if one occurs
     * according to {@link FileConfiguration#save(File)}.
     *
     * @throws IOException if an IOException is thrown by {@link FileConfiguration#save(File)}
     */
    public void saveExceptionally() throws IOException {
        this.config.save(file);
    }

    /**
     * Save this {@link ConfigWrapper}.
     */
    public void save() {
        try {
            this.saveExceptionally();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reload values from file into memory.
     */
    public void reload() {
        this.config = YamlConfiguration.loadConfiguration(file);

        // Loading defaults if necessary
        final InputStream defaultConfigStream = plugin.getResource(rawPath);
        if (defaultConfigStream == null) {
            return;
        }

        this.config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defaultConfigStream, Charsets.UTF_8)));
    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof ConfigWrapper other && file.equals(other.file));
    }

}
