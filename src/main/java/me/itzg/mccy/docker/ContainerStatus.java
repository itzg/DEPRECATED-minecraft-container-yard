package me.itzg.mccy.docker;

/**
 * @author Geoff Bourne
 * @since 3/13/2015
 */
public enum ContainerStatus {
    RESTARTING("restarting"),
    RUNNING("running"),
    PAUSED("paused"),
    EXITED("exited");

    private final String encoded;

    ContainerStatus(String encoded) {
        this.encoded = encoded;
    }

    public String getEncoded() {
        return encoded;
    }
}
