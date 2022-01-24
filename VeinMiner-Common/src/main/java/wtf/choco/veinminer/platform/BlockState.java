package wtf.choco.veinminer.platform;

import org.jetbrains.annotations.NotNull;

public interface BlockState {

    @NotNull
    public BlockType getType();

    @NotNull
    public String getAsString(boolean hideUnspecified);

    public boolean matches(@NotNull BlockState state);

}
