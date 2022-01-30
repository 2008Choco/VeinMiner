package wtf.choco.veinminer.data;

import com.google.common.base.Enums;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
 * A general abstract implementation of SQL-based {@link PersistentDataStorage}.
 *
 * @see PersistentDataStorageMySQL
 * @see PersistentDataStorageSQLite
 */
public abstract class PersistentDataStorageSQL implements PersistentDataStorage { // Java 17: Sealed classes

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
    public final CompletableFuture<VeinMinerPlayer> save(@NotNull VeinMinerPlugin plugin, @NotNull VeinMinerPlayer player) {
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
    public CompletableFuture<List<VeinMinerPlayer>> save(@NotNull VeinMinerPlugin plugin, @NotNull Collection<? extends VeinMinerPlayer> players) {
        if (players.isEmpty() || players.stream().allMatch(Predicates.not(VeinMinerPlayer::isDirty))) {
            return CompletableFuture.completedFuture(new ArrayList<>(players));
        }

        return CompletableFuture.supplyAsync(() -> {
            List<VeinMinerPlayer> result = new ArrayList<>();

            try (Connection connection = openConnection()) {
                PreparedStatement statement = connection.prepareStatement(getInsertPlayerDataStatement());

                int dispatched = 0;
                for (VeinMinerPlayer player : players) {
                    this.writeToSaveStatement(statement, player);
                    statement.addBatch();

                    // If we reach over 100 updates to perform, send off a batch to start processing while we finish off the rest
                    if (++dispatched % 100 == 0) {
                        statement.executeBatch();
                        dispatched = 0;
                    }

                    result.add(player);
                }

                if (dispatched > 0) {
                    statement.executeBatch();
                }
            } catch (SQLException e) {
                throw new CompletionException(e);
            }

            return result;
        });
    }

    @NotNull
    @Override
    public final CompletableFuture<VeinMinerPlayer> load(@NotNull VeinMinerPlugin plugin, @NotNull VeinMinerPlayer player) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = openConnection()) {
                PreparedStatement statement = connection.prepareStatement(getSelectAllPlayerDataQuery());
                statement.setString(1, player.getPlayerUUID().toString());

                ResultSet result = statement.executeQuery();
                if (result.next()) {
                    this.handleResultSet(plugin, player, result);
                }

                return player;
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });
    }

    @NotNull
    @Override
    public CompletableFuture<List<VeinMinerPlayer>> load(@NotNull VeinMinerPlugin plugin, @NotNull Collection<? extends VeinMinerPlayer> players) {
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
                    this.handleResultSet(plugin, player, resultSet);
                }
            } catch (SQLException e) {
                throw new CompletionException(e);
            }

            return result;
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

    private void writeToSaveStatement(PreparedStatement statement, VeinMinerPlayer player) throws SQLException {
        statement.setString(1, player.getPlayerUUID().toString());
        statement.setString(2, player.getActivationStrategy().name());
        Set<VeinMinerToolCategory> disabledCategories = player.getDisabledCategories();
        statement.setString(3, disabledCategories.isEmpty() ? null : String.join(",", Collections2.transform(disabledCategories, VeinMinerToolCategory::getId)));
        statement.setString(4, player.getVeinMiningPattern().getKey().toString());
    }

    private VeinMinerPlayer handleResultSet(VeinMinerPlugin plugin, VeinMinerPlayer player, ResultSet result) throws SQLException {
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
        return player;
    }

}
