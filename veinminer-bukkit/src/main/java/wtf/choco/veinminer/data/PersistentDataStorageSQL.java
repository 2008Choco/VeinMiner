package wtf.choco.veinminer.data;

import com.google.common.base.Enums;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.pattern.VeinMiningPattern;
import wtf.choco.veinminer.player.ActivationStrategy;
import wtf.choco.veinminer.player.VeinMinerPlayer;
import wtf.choco.veinminer.tool.VeinMinerToolCategory;

/**
 * A general abstract implementation of SQL-based {@link PersistentDataStorage}.
 *
 * @see PersistentDataStorageMySQL
 * @see PersistentDataStorageSQLite
 */
abstract sealed class PersistentDataStorageSQL implements PersistentDataStorage, LegacyImportable permits PersistentDataStorageMySQL, PersistentDataStorageSQLite {

    private final PersistentStorageType type;
    private final VeinMinerPlugin plugin;

    PersistentDataStorageSQL(@NotNull PersistentStorageType type, @NotNull VeinMinerPlugin plugin) {
        this.type = type;
        this.plugin = plugin;
    }

    @NotNull
    @Override
    public PersistentStorageType getType() {
        return type;
    }

    @NotNull
    @Override
    public final CompletableFuture<Void> init() {
        return CompletableFuture.runAsync(() -> {
            try {
                this.initDriver();
            } catch (ClassNotFoundException e) {
                throw new CompletionException(e);
            }
        }).thenRun(() -> {
            try (Connection connection = openConnection()) {
                connection.createStatement().execute(getCreatePlayersTableStatement());
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });
    }

    @NotNull
    @Override
    public final CompletableFuture<VeinMinerPlayer> save(@NotNull VeinMinerPlayer player) {
        if (!player.isDirty()) {
            return CompletableFuture.completedFuture(player);
        }

        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = openConnection()) {
                PreparedStatement statement = connection.prepareStatement(getInsertPlayerDataStatement());
                this.writeToSaveStatement(statement, player);
                statement.execute();

                return player;
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });
    }

    @NotNull
    @Override
    public CompletableFuture<List<VeinMinerPlayer>> save(@NotNull Collection<? extends VeinMinerPlayer> players) {
        if (players.isEmpty() || players.stream().allMatch(player -> !player.isDirty())) {
            return CompletableFuture.completedFuture(new ArrayList<>(players));
        }

        return CompletableFuture.supplyAsync(() -> {
            List<VeinMinerPlayer> result = new ArrayList<>();

            try (Connection connection = openConnection()) {
                connection.setAutoCommit(false);
                PreparedStatement statement = connection.prepareStatement(getInsertPlayerDataStatement());

                for (VeinMinerPlayer player : players) {
                    this.writeToSaveStatement(statement, player);
                    statement.execute();

                    result.add(player);
                }

                connection.commit();
            } catch (SQLException e) {
                throw new CompletionException(e);
            }

            return result;
        });
    }

    @NotNull
    @Override
    public final CompletableFuture<VeinMinerPlayer> load(@NotNull VeinMinerPlayer player) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = openConnection()) {
                PreparedStatement statement = connection.prepareStatement(getSelectAllPlayerDataQuery());
                statement.setString(1, player.getPlayerUUID().toString());

                ResultSet result = statement.executeQuery();
                if (result.next()) {
                    this.handleResultSet(player, result);
                }

                return player;
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });
    }

    @NotNull
    @Override
    public CompletableFuture<List<VeinMinerPlayer>> load(@NotNull Collection<? extends VeinMinerPlayer> players) {
        if (players.isEmpty()) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        return CompletableFuture.supplyAsync(() -> {
            List<VeinMinerPlayer> result = new ArrayList<>();

            try (Connection connection = openConnection()) {
                PreparedStatement statement = connection.prepareStatement(getSelectAllPlayerDataBatchQuery(players.size()));

                int index = 0;
                for (VeinMinerPlayer player : players) {
                    statement.setString(++index, player.getPlayerUUID().toString());
                    result.add(player);
                }

                ResultSet resultSet = statement.executeQuery();

                index = 0;
                while (resultSet.next()) {
                    VeinMinerPlayer player = result.get(index++);
                    this.handleResultSet(player, resultSet);
                }
            } catch (SQLException e) {
                throw new CompletionException(e);
            }

            return result;
        });
    }

    @NotNull
    @Override
    public CompletableFuture<Integer> importLegacyData(@NotNull List<LegacyPlayerData> data) {
        if (data.isEmpty()) {
            return CompletableFuture.completedFuture(0);
        }

        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = openConnection()) {
                connection.setAutoCommit(false);
                PreparedStatement statement = connection.prepareStatement(getInsertPlayerDataStatement());

                for (LegacyPlayerData playerData : data) {
                    this.writeToImportSaveStatement(statement, playerData);
                    statement.execute();
                }

                connection.commit();
            } catch (SQLException e) {
                throw new CompletionException(e);
            }

            return data.size();
        });
    }

    /**
     * Initialize the SQL driver required for this implementation.
     *
     * @throws ClassNotFoundException if the driver class could not be found
     *
     * @implSpec this should load and initialize the driver with a class load if necessary
     */
    protected abstract void initDriver() throws ClassNotFoundException;

    /**
     * Open a new {@link Connection} to the database.
     *
     * @return a new connection
     *
     * @throws SQLException if an SQL exception occurred while opening the connection
     */
    @NotNull
    protected abstract Connection openConnection() throws SQLException;

    /**
     * Get the statement used to create the players table.
     *
     * @return the create table statement
     */
    @NotNull
    protected abstract String getCreatePlayersTableStatement();

    /**
     * Get the statement used to insert a player's data into the players table.
     *
     * @return the insert player data statement
     */
    @NotNull
    protected abstract String getInsertPlayerDataStatement();

    /**
     * Get the statement used to select all of a player's data from the players table.
     *
     * @return the select player data statement
     */
    @NotNull
    protected abstract String getSelectAllPlayerDataQuery();

    /**
     * Get the statement used to select multiple players' data from the players table.
     *
     * @param count the amount of players that need selecting
     *
     * @return the select player data statement
     */
    @NotNull
    protected abstract String getSelectAllPlayerDataBatchQuery(int count);

    private void writeToSaveStatement(PreparedStatement statement, UUID playerUUID, ActivationStrategy activationStrategy, Collection<VeinMinerToolCategory> disabledCategories, VeinMiningPattern pattern) throws SQLException {
        statement.setString(1, playerUUID.toString());
        statement.setString(2, activationStrategy.name());

        String disabledCategoriesString = (disabledCategories != null) ? disabledCategories.stream().map(VeinMinerToolCategory::getId).collect(Collectors.joining(",")) : null;
        statement.setString(3, disabledCategories == null || disabledCategories.isEmpty() ? null : disabledCategoriesString);

        statement.setString(4, (pattern != null) ? pattern.getKey().toString() : null);
    }

    private void writeToSaveStatement(PreparedStatement statement, VeinMinerPlayer player) throws SQLException {
        this.writeToSaveStatement(statement, player.getPlayerUUID(), player.getActivationStrategy(), player.getDisabledCategories(), player.getVeinMiningPattern());
    }

    private void writeToImportSaveStatement(PreparedStatement statement, LegacyPlayerData data) throws SQLException {
        this.writeToSaveStatement(statement, data.playerUUID(), data.activationStrategy(), data.disabledCategories(), null);
    }

    private VeinMinerPlayer handleResultSet(VeinMinerPlayer player, ResultSet result) throws SQLException {
        String activationStrategyId = result.getString("activation_strategy_id");
        String disabledCategories = result.getString("disabled_categories");
        String veinMiningPatternId = result.getString("vein_mining_pattern_id");

        if (activationStrategyId != null) {
            player.setActivationStrategy(Enums.getIfPresent(ActivationStrategy.class, activationStrategyId.toUpperCase()).or(plugin.getConfiguration().getDefaultActivationStrategy()));
        }

        if (disabledCategories != null) {
            player.setVeinMinerEnabled(true); // Ensure that all categories are loaded again

            for (String categoryId : disabledCategories.split(",")) {
                VeinMinerToolCategory category = plugin.getToolCategoryRegistry().get(categoryId.toUpperCase());

                if (category == null) {
                    continue;
                }

                player.setVeinMinerEnabled(category, false);
            }
        }

        if (veinMiningPatternId != null) {
            VeinMiningPattern pattern = plugin.getPatternRegistry().get(veinMiningPatternId);
            player.setVeinMiningPattern(pattern != null ? pattern : plugin.getConfiguration().getDefaultVeinMiningPattern(), false);
        }

        player.setDirty(false); // They are no longer dirty. We just loaded them
        return player;
    }

}
