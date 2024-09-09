package wtf.choco.veinminer.data;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.config.VeinMinerConfiguration;

/**
 * A type of persistent data storage.
 */
public final class PersistentStorageType {

    private static final Map<String, PersistentStorageType> TYPE_BY_NAME = new HashMap<>();

    /**
     * Storage in JSON data files.
     */
    public static final PersistentStorageType JSON = new PersistentStorageType("json", PersistentStorageType::json);
    /**
     * Storage in a remote MySQL database.
     */
    public static final PersistentStorageType MYSQL = new PersistentStorageType("mysql", PersistentStorageType::mysql);
    /**
     * Storage in a local SQLite database file.
     */
    public static final PersistentStorageType SQLITE = new PersistentStorageType("sqlite", PersistentStorageType::sqlite);
    /**
     * No persistent storage.
     */
    public static final PersistentStorageType NONE = new PersistentStorageType("none", ignore -> PersistentDataStorageNoOp.INSTANCE);

    private final String name;
    private final Function<VeinMinerPlugin, PersistentDataStorage> configurator;

    private PersistentStorageType(String name, Function<VeinMinerPlugin, PersistentDataStorage> configurator) {
        this.name = name;
        this.configurator = configurator;
        TYPE_BY_NAME.put(name, this);
    }

    /**
     * Get the friendly name of this persistent storage type. No format is enforced.
     *
     * @return the name
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Create a new {@link PersistentDataStorage} instance from this type.
     *
     * @param plugin the plugin instance
     *
     * @return the new persistent storage instance, which must be initialized with
     * {@link PersistentDataStorage#init()}
     */
    @NotNull
    public PersistentDataStorage createStorage(@NotNull VeinMinerPlugin plugin) {
        return configurator.apply(plugin);
    }

    /**
     * Get a {@link PersistentStorageType} by its (case-insensitive) name.
     *
     * @param name the name of the storage type
     *
     * @return the persistent storage type, or {@link #NONE} if none match
     */
    @NotNull
    public static PersistentStorageType getByName(@NotNull String name) {
        return TYPE_BY_NAME.getOrDefault(name.toLowerCase(), NONE);
    }

    @NotNull
    private static PersistentDataStorage json(@NotNull VeinMinerPlugin plugin) {
        File jsonDirectory = plugin.getConfiguration().getJsonStorageDirectory();

        if (jsonDirectory == null) {
            throw new IllegalStateException("Incomplete configuration for JSON persistent storage. Requires a valid directory");
        }

        return new PersistentDataStorageJSON(JSON, plugin, jsonDirectory);
    }

    @NotNull
    private static PersistentDataStorage mysql(@NotNull VeinMinerPlugin plugin) {
        VeinMinerConfiguration config = plugin.getConfiguration();

        String host = config.getMySQLHost();
        int port = config.getMySQLPort();
        String username = config.getMySQLUsername();
        String password = config.getMySQLPassword();
        String database = config.getMySQLDatabase();
        String tablePrefix = config.getMySQLTablePrefix();

        if (host == null || database == null || username == null || password == null || tablePrefix == null) {
            throw new IllegalStateException("Incomplete configuration for MySQL persistent storage. Requires a valid host, port, database, username, password, and table prefix.");
        }

        return new PersistentDataStorageMySQL(MYSQL, plugin, host, port, username, password, database, tablePrefix);
    }

    @NotNull
    private static PersistentDataStorage sqlite(@NotNull VeinMinerPlugin plugin) {
        try {
            return new PersistentDataStorageSQLite(SQLITE, plugin, plugin.getDataFolder().toPath(), "veinminer.db");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
