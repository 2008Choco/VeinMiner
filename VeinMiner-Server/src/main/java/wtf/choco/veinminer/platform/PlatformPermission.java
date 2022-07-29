package wtf.choco.veinminer.platform;

public interface PlatformPermission {

    public String getName();

    public void addChild(PlatformPermission permission, boolean value);

    public void recalculatePermissibles();

    public static enum Default {

        TRUE,
        FALSE,
        OP,
        NOT_OP;

    }

}
