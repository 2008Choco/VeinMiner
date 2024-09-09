package wtf.choco.veinminer.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.function.IntFunction;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMinerPlugin;

/**
 * An implementation of {@link PersistentDataStorage} for MySQL servers.
 */
final class PersistentDataStorageMySQL extends PersistentDataStorageSQL {

    private static final String CREATE_TABLE_PLAYERS = """
            CREATE TABLE IF NOT EXISTS %prefix%player_data (
                player_id                INTEGER    NOT NULL AUTO_INCREMENT,
                player_uuid              CHAR(36)   NOT NULL UNIQUE,
                activation_strategy_id   VARCHAR(16),
                disabled_categories      VARCHAR(256),
                vein_mining_pattern_id   VARCHAR(48),
                PRIMARY KEY (player_id)
            )
            """;

    private static final String INSERT_PLAYER_DATA = """
            INSERT INTO %prefix%player_data(
                player_uuid,
                activation_strategy_id,
                disabled_categories,
                vein_mining_pattern_id
            ) VALUES(?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                activation_strategy_id = VALUES(activation_strategy_id),
                disabled_categories = VALUES(disabled_categories),
                vein_mining_pattern_id = VALUES(vein_mining_pattern_id)
            """;

    private static final String SELECT_PLAYER_DATA = """
            SELECT * FROM %prefix%player_data WHERE player_uuid = ?
            """;

    // SELECT * FROM %prefix%player_data WHERE player_uuid IN (?, ?, ?, ?, ...)
    private static final IntFunction<String> SELECT_PLAYER_DATA_BATCH = count -> SELECT_PLAYER_DATA.replace("= ?", "IN (" + String.join(", ", Collections.nCopies(count, "?")) + ")");

    private final String connectionURL;
    private final String username, password;
    private final String tablePrefix;

    PersistentDataStorageMySQL(@NotNull PersistentStorageType type, @NotNull VeinMinerPlugin plugin, @NotNull String host, int port, @NotNull String username, @NotNull String password, @NotNull String database, @NotNull String tablePrefix) {
        super(type, plugin);

        this.connectionURL = String.format("jdbc:mysql://%s:%d/%s", host, port, database);
        this.username = username;
        this.password = password;
        this.tablePrefix = tablePrefix;
    }

    @Override
    protected void initDriver() throws ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
    }

    @NotNull
    @Override
    protected Connection openConnection() throws SQLException {
        return DriverManager.getConnection(connectionURL, username, password);
    }

    @NotNull
    @Override
    protected String getCreatePlayersTableStatement() {
        return CREATE_TABLE_PLAYERS.replace("%prefix%", tablePrefix);
    }

    @NotNull
    @Override
    protected String getInsertPlayerDataStatement() {
        return INSERT_PLAYER_DATA.replace("%prefix%", tablePrefix);
    }

    @NotNull
    @Override
    protected String getSelectAllPlayerDataQuery() {
        return SELECT_PLAYER_DATA.replace("%prefix%", tablePrefix);
    }

    @NotNull
    @Override
    protected String getSelectAllPlayerDataBatchQuery(int count) {
        return SELECT_PLAYER_DATA_BATCH.apply(count).replace("%prefix%", tablePrefix);
    }

}
