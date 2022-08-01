package wtf.choco.veinminer.data;

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

import wtf.choco.veinminer.ActivationStrategy;
import wtf.choco.veinminer.VeinMinerPlayer;
import wtf.choco.veinminer.VeinMinerServer;
import wtf.choco.veinminer.pattern.VeinMiningPattern;
import wtf.choco.veinminer.tool.VeinMinerToolCategory;
import wtf.choco.veinminer.util.EnumUtil;

/**
 * A general abstract implementation of SQL-based {@link PersistentDataStorage}.
 *
 * @see PersistentDataStorageMySQL
 * @see PersistentDataStorageSQLite
 */
public abstract class PersistentDataStorageSQL implements PersistentDataStorage { // Java 17: Sealed classes

    private final VeinMinerServer veinMiner;

    /**
     * Construct a new {@link PersistentDataStorageSQL}.
     *
     * @param veinMiner the vein miner server
     */
    public PersistentDataStorageSQL(@NotNull VeinMinerServer veinMiner) {
        this.veinMiner = veinMiner;
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

    /**
     * Import legacy data into the SQL database. This makes no attempt at preserving existing
     * entries in the database.
     *
     * @param data the data to import
     *
     * @return a completable future, completed when the import has finished
     */
    @NotNull
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

    protected abstract void initDriver() throws ClassNotFoundException;

    @NotNull
    protected abstract Connection openConnection() throws SQLException;

    @NotNull
    protected abstract String getCreatePlayersTableStatement();

    @NotNull
    protected abstract String getInsertPlayerDataStatement();

    @NotNull
    protected abstract String getSelectAllPlayerDataQuery();

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
            player.setActivationStrategy(EnumUtil.get(ActivationStrategy.class, activationStrategyId.toUpperCase()).orElse(veinMiner.getDefaultActivationStrategy()));
        }

        if (disabledCategories != null) {
            player.setVeinMinerEnabled(true); // Ensure that all categories are loaded again

            for (String categoryId : disabledCategories.split(",")) {
                VeinMinerToolCategory category = veinMiner.getToolCategoryRegistry().get(categoryId.toUpperCase());

                if (category == null) {
                    continue;
                }

                player.setVeinMinerEnabled(category, false);
            }
        }

        if (veinMiningPatternId != null) {
            VeinMiningPattern pattern = veinMiner.getPatternRegistry().get(veinMiningPatternId);
            player.setVeinMiningPattern(pattern != null ? pattern : veinMiner.getDefaultVeinMiningPattern(), false);
        }

        player.setDirty(false); // They are no longer dirty. We just loaded them
        return player;
    }

}
