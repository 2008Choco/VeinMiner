package wtf.choco.veinminer.platform;

import org.bukkit.permissions.Permission;

/**
 * Bukkit implementation of {@link PlatformPermission}.
 */
public final class BukkitPlatformPermission implements PlatformPermission {

    private final Permission permission;

    BukkitPlatformPermission(Permission permission) {
        this.permission = permission;
    }

    @Override
    public String getName() {
        return permission.getName();
    }

    @Override
    public void addChild(PlatformPermission permission, boolean value) {
        this.permission.getChildren().put(permission.getName(), value);
    }

    @Override
    public void recalculatePermissibles() {
        this.permission.recalculatePermissibles();
    }

}
