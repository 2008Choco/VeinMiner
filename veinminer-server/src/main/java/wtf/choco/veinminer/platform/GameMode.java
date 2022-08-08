package wtf.choco.veinminer.platform;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a player's game mode.
 */
public enum GameMode {

    /**
     * Survival mode.
     */
    SURVIVAL("survival"),
    /**
     * Creative mode.
     */
    CREATIVE("creative"),
    /**
     * Adventure mode.
     */
    ADVENTURE("adventure"),
    /**
     * Spectator mode.
     */
    SPECTATOR("spectator");

    private static final Map<String, GameMode> BY_ID = new HashMap<>();

    static {
        for (GameMode gameMode : GameMode.values()) {
            BY_ID.put(gameMode.getId(), gameMode);
        }
    }

    private final String id;

    private GameMode(@NotNull String id) {
        this.id = id;
    }

    /**
     * Get the id of this game mode.
     *
     * @return the id
     */
    @NotNull
    public String getId() {
        return id;
    }

    /**
     * Get a {@link GameMode} by its (case-insensitive) string id.
     *
     * @param id the id of the game mode
     *
     * @return the game mode
     */
    @Nullable
    public static GameMode getById(@NotNull String id) {
        return BY_ID.get(id.toLowerCase());
    }

    /**
     * Get a {@link GameMode} by its (case-insensitive) string id. If a {@link GameMode}
     * with the given id does not exist, an exception will be thrown.
     *
     * @param id the id of the game mode
     *
     * @return the game mode
     *
     * @throws IllegalArgumentException if the game mode does not exist
     */
    @NotNull
    public static GameMode getByIdOrThrow(@NotNull String id) {
        GameMode gameMode = getById(id);

        if (gameMode == null) {
            throw new IllegalArgumentException(String.format("Unknown GameMode with id \"%s\"", id));
        }

        return gameMode;
    }

}
