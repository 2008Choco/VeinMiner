package wtf.choco.veinminer.data;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.base.Preconditions;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import wtf.choco.veinminer.pattern.PatternDefault;
import wtf.choco.veinminer.pattern.VeinMiningPattern;
import wtf.choco.veinminer.tool.ToolCategory;

public final class VMPlayerData {

	private static final Map<UUID, VMPlayerData> CACHE = new HashMap<>();

	private VeinMiningPattern pattern = PatternDefault.get();
	private final Set<ToolCategory> disabledCategories = EnumSet.noneOf(ToolCategory.class);

	private final UUID player;

	private VMPlayerData(UUID player) {
		this.player = player;
	}

	public OfflinePlayer getPlayer() {
		return Bukkit.getOfflinePlayer(player);
	}

	public Player getPlayerOnline() {
		return Bukkit.getPlayer(player);
	}

	public VeinMiningPattern getPattern() {
		return pattern;
	}

	public void setPattern(VeinMiningPattern pattern) {
		this.pattern = (pattern != null) ? pattern : PatternDefault.get();
	}

	public void enableVeinMiner() {
		this.disabledCategories.clear();
	}

	public void enableVeinMiner(ToolCategory category) {
		Preconditions.checkArgument(category != null, "Cannot enable null category");
		this.disabledCategories.remove(category);
	}

	public void disableVeinMiner() {
		for (ToolCategory category : ToolCategory.values()) {
			this.disabledCategories.add(category);
		}
	}

	public void disableVeinMiner(ToolCategory category) {
		Preconditions.checkArgument(category != null, "Cannot disable null category");
		this.disabledCategories.add(category);
	}

	public void setVeinMinerEnabled(boolean enabled) {
		if (enabled) {
			this.enableVeinMiner();
		} else {
			this.disableVeinMiner();
		}
	}

	public void setVeinMinerEnabled(boolean enabled, ToolCategory category) {
		if (enabled) {
			this.enableVeinMiner(category);
		} else {
			this.disableVeinMiner(category);
		}
	}

	public boolean isVeinMinerEnabled() {
		return disabledCategories.isEmpty();
	}

	public boolean isVeinMinerEnabled(ToolCategory category) {
		return !disabledCategories.contains(category);
	}

	public boolean isVeinMinerDisabled() {
		return disabledCategories.size() >= ToolCategory.values().length;
	}

	public boolean isVeinMinerDisabled(ToolCategory category) {
		return disabledCategories.contains(category);
	}

	public boolean isVeinMinerPartiallyDisabled() {
		return !disabledCategories.isEmpty();
	}

	public static VMPlayerData get(OfflinePlayer player) {
		Preconditions.checkArgument(player != null, "Cannot get data for null player");
		return CACHE.computeIfAbsent(player.getUniqueId(), VMPlayerData::new);
	}

	public static void clearCache() {
		CACHE.clear();
	}

}