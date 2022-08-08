package wtf.choco.veinminer.platform;

/**
 * Represents a server permission node.
 */
public interface PlatformPermission {

    /**
     * Get the name (or "id") of this permission node.
     *
     * @return the name
     */
    public String getName();

    /**
     * Add the given {@link PlatformPermission} as a child of this permission node.
     *
     * @param permission the child permission to add
     * @param value whether or not the child permission will inherit the parent permission. If
     * false, the child will inherit the inverse of its parent permission
     */
    public void addChild(PlatformPermission permission, boolean value);

    /**
     * Recalculate all permissible objects that have this permission attached to them.
     */
    public void recalculatePermissibles();

    /**
     * The group to which this permission should be attributed to by default if not explicitly set.
     */
    public static enum Default {

        /**
         * Attributed to all permissibles.
         */
        TRUE,
        /**
         * Not attributed to any permissibles.
         */
        FALSE,
        /**
         * Only attributed to operator permissibles.
         */
        OP,
        /**
         * Only attributed to non-operator permissibles.
         */
        NOT_OP;

    }

}
