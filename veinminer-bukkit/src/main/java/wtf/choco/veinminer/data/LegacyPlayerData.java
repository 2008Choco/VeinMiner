package wtf.choco.veinminer.data;

import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.player.ActivationStrategy;
import wtf.choco.veinminer.tool.VeinMinerToolCategory;

/**
 * Represents simple legacy player data for the {@link LegacyImportTask}.
 *
 * @param playerUUID the player UUID
 * @param activationStrategy the activation strategy
 * @param disabledCategories the disabled categories
 */
record LegacyPlayerData(@NotNull UUID playerUUID, @NotNull ActivationStrategy activationStrategy, @NotNull List<VeinMinerToolCategory> disabledCategories) { }
