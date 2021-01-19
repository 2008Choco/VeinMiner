package wtf.choco.veinminer.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a pair of two (potentially null) objects.
 *
 * @param <L> the type of object on the left
 * @param <R> the type of object on the right
 *
 * @author Parker Hawke - Choco
 */
public class Pair<L, R> {

    private final L left;
    private final R right;

    /**
     * Construct a new pair of objects.
     *
     * @param left the left object
     * @param right the right object
     */
    public Pair(@Nullable L left, @Nullable R right) {
        this.left = left;
        this.right = right;
    }

    /**
     * Get the object on the left.
     *
     * @return the left object. null if none
     */
    @Nullable
    public L getLeft() {
        return left;
    }

    /**
     * Get the object on the right.
     *
     * @return the right object. null if none
     */
    @Nullable
    public R getRight() {
        return right;
    }

    /**
     * Check whether or not this pair is empty. A pair is empty if both elements are null.
     *
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return left == null && right == null;
    }

    /**
     * Get an empty pair.
     *
     * @param <L> the type of object on the left
     * @param <R> the type of object on the right
     *
     * @return an empty pair
     */
    @NotNull
    public static <L, R> Pair<L, R> empty() {
        return new Pair<>(null, null);
    }

}