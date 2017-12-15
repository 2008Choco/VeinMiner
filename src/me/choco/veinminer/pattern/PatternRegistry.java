package me.choco.veinminer.pattern;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import org.bukkit.NamespacedKey;

import me.choco.veinminer.VeinMiner;

/**
 * A registry to register any custom implementations of {@link VeinMiningPattern}
 */
public class PatternRegistry {
	
	/**
	 * The default mining pattern for VeinMiner
	 */
	public static final VeinMiningPattern VEINMINER_PATTERN_DEFAULT = new PatternDefault(VeinMiner.getPlugin());
	
	private Map<NamespacedKey, VeinMiningPattern> patterns = new HashMap<>();
	
	public PatternRegistry() {
		this.patterns.put(VEINMINER_PATTERN_DEFAULT.getKey(), VEINMINER_PATTERN_DEFAULT);
	}
	
	/**
	 * Register a new VeinMiningPattern implementation
	 * 
	 * @param pattern the pattern to register
	 */
	public void registerPattern(VeinMiningPattern pattern) {
		Preconditions.checkArgument(pattern != null, "Cannot register a null pattern");
		Preconditions.checkArgument(pattern.getKey() != null, "Vein mining patterns must have a non-null key");
		Preconditions.checkArgument(patterns.containsKey(pattern.getKey()), "Patterns must have a unique key (%s)", pattern.getKey().toString());
		
		this.patterns.put(pattern.getKey(), pattern);
	}
	
	/**
	 * Get the pattern associated with the given key
	 * 
	 * @param key the key of the pattern to retrieve
	 * @return the pattern. null if no pattern matches the given key
	 */
	public VeinMiningPattern getPattern(NamespacedKey key) {
		return patterns.get(key);
	}
	
	/**
	 * Get the pattern associated with the given key or default if one is not registered
	 * 
	 * @param key the key of the pattern to retrieve
	 * @param defaultPattern the default pattern in the case the key is not registered
	 * 
	 * @return the pattern. The default pattern if no pattern matches the given key
	 */
	public VeinMiningPattern getPatternOrDefault(NamespacedKey key, VeinMiningPattern defaultPattern) {
		return patterns.getOrDefault(key, defaultPattern);
	}
	
	/**
	 * Get a pattern associated with the given key (in the form of a String)
	 * 
	 * @param key the key of the pattern to retrieve
	 * @return the pattern. null if no pattern matches the given key
	 */
	public VeinMiningPattern getPattern(String key) {
		return getPatternOrDefault(key, null);
	}
	
	/**
	 * Get the pattern associated with the given key (in the form of a String) or default if
	 * one is not registered
	 * 
	 * @param key the key of the pattern to retrieve
	 * @param defaultPattern the default pattern in the case the key is not registered
	 * 
	 * @return the pattern. The default pattern if no pattern matches the given key
	 */
	public VeinMiningPattern getPatternOrDefault(String key, VeinMiningPattern defaultPattern) {
		for (Entry<NamespacedKey, VeinMiningPattern> entry : patterns.entrySet()) {
			if (entry.getKey().toString().equals(key)) {
				return entry.getValue();
			}
		}
		
		return defaultPattern;
	}
	
	/**
	 * Unregister the provided pattern from the pattern registry
	 * 
	 * @param pattern the pattern to unregister
	 */
	public void unregisterPattern(VeinMiningPattern pattern) {
		if (pattern == null) return;
		this.patterns.remove(pattern.getKey());
	}
	
	/**
	 * Unregister the pattern associated with the given key from the pattern registry
	 * 
	 * @param key the key of the pattern to unregister
	 */
	public void unregisterPattern(NamespacedKey key) {
		this.patterns.remove(key);
	}
	
	/**
	 * Get an immutable Set of all registered patterns
	 * 
	 * @return all registered patterns
	 */
	public Set<VeinMiningPattern> getPatterns() {
		return ImmutableSet.copyOf(patterns.values());
	}
	
	/**
	 * Clear all patterns from the registry
	 */
	public void clearPatterns() {
		this.patterns.clear();
	}
	
}