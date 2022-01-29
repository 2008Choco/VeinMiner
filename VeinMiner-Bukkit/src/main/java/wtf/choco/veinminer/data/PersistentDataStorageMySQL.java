package wtf.choco.veinminer.data;

import com.google.common.base.Enums;
import com.google.common.collect.Collections2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.ActivationStrategy;
import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.network.VeinMinerPlayer;
import wtf.choco.veinminer.pattern.VeinMiningPattern;
import wtf.choco.veinminer.tool.VeinMinerToolCategory;

/**
 * An implementation of {@link PersistentDataStorage} for MySQL servers.
 */
public final class PersistentDataStorageMySQL implements PersistentDataStorage {

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
            SELECT * FROM %prefix%player_data players WHERE player_uuid = ?
            """;

    private final String connectionURL;
    private final String username, password;
    private final String tablePrefix;

    /**
     * Construct a new {@link PersistentDataStorageMySQL}.
     *
     * @param host the server host
     * @param port the server port
     * @param username the username
     * @param password the password
     * @param database the database to use
     * @param tablePrefix the prefix for all tables created by this implementation
     */
    public PersistentDataStorageMySQL(@NotNull String host, int port, @NotNull String username, @NotNull String password, @NotNull String database, @NotNull String tablePrefix) {
        this.connectionURL = String.format("jdbc:mysql://%s:%d/%s", host, port, database);
        this.username = username;
        this.password = password;
        this.tablePrefix = tablePrefix;
    }

    @NotNull
    @Override
    public Type getType() {
        return Type.MYSQL;
    }

    @NotNull
    @Override
    public CompletableFuture<Void> init() {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = openConnection()) {
                connection.createStatement().execute(CREATE_TABLE_PLAYERS.replace("%prefix%", tablePrefix));
            } catch (SQLException | ClassNotFoundException e) {
                throw new CompletionException(e);
            }
        });
    }

    @NotNull
    @Override
    public CompletableFuture<VeinMinerPlayer> save(@NotNull VeinMinerPlugin plugin, @NotNull VeinMinerPlayer player) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = openConnection()) {
                PreparedStatement statement = connection.prepareStatement(INSERT_PLAYER_DATA.replace("%prefix%", tablePrefix));
                statement.setString(1, player.getPlayerUUID().toString());
                statement.setString(2, player.getActivationStrategy().name());
                Set<VeinMinerToolCategory> disabledCategories = player.getDisabledCategories();
                statement.setString(3, disabledCategories.isEmpty() ? null : String.join(",", Collections2.transform(disabledCategories, VeinMinerToolCategory::getId)));
                statement.setString(4, player.getVeinMiningPattern().getKey().toString());
                statement.execute();
            } catch (SQLException | ClassNotFoundException e) {
                throw new CompletionException(e);
            }

            return player;
        });
    }

    @NotNull
    @Override
    public CompletableFuture<VeinMinerPlayer> load(@NotNull VeinMinerPlugin plugin, @NotNull VeinMinerPlayer player) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = openConnection()) {
                PreparedStatement statement = connection.prepareStatement(SELECT_PLAYER_DATA.replace("%prefix%", tablePrefix));
                statement.setString(1, player.getPlayerUUID().toString());

                ResultSet result = statement.executeQuery();

                if (result.next()) {
                    String activationStrategyId = result.getString("activation_strategy_id");
                    String disabledCategories = result.getString("disabled_categories");
                    String veinMiningPatternId = result.getString("vein_mining_pattern_id");

                    if (activationStrategyId != null) {
                        player.setActivationStrategy(Enums.getIfPresent(ActivationStrategy.class, activationStrategyId.toUpperCase()).or(VeinMiner.getInstance().getDefaultActivationStrategy()));
                    }

                    if (disabledCategories != null) {
                        player.enableVeinMiner(); // Ensure that all categories are loaded again

                        for (String categoryId : disabledCategories.split(",")) {
                            VeinMinerToolCategory category = plugin.getToolCategoryRegistry().get(categoryId.toUpperCase());

                            if (category == null) {
                                continue;
                            }

                            player.disableVeinMiner(category);
                        }
                    }

                    if (veinMiningPatternId != null) {
                        VeinMiningPattern pattern = plugin.getPatternRegistry().get(veinMiningPatternId);
                        player.setVeinMiningPattern(pattern != null ? pattern : plugin.getDefaultVeinMiningPattern());
                    }

                    player.setDirty(false); // They are no longer dirty. We just loaded them
                }
            } catch (SQLException | ClassNotFoundException e) {
                throw new CompletionException(e);
            }

            return player;
        });
    }

    private Connection openConnection() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
        return DriverManager.getConnection(connectionURL, username, password);
    }

}
