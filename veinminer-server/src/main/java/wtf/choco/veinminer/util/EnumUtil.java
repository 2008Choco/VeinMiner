package wtf.choco.veinminer.util;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

/**
 * A utility class to more easily operate with enums.
 */
public final class EnumUtil {

    private EnumUtil() { }

    /**
     * Get an enum constant with the given name if it exists.
     *
     * @param enumClass the class of the enum from which to get a constant
     * @param name the name of the constant. This name is case-insensitive
     * @param <E> the enum type
     *
     * @return an {@link Optional} containing the constant, or an empty Optional if a constant
     * with the given name did not exist
     */
    @NotNull
    public static <E extends Enum<E>> Optional<E> get(@NotNull Class<E> enumClass, @NotNull String name) {
        try {
            return Optional.ofNullable(Enum.valueOf(enumClass, name.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

}
