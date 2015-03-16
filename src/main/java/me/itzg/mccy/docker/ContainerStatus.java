package me.itzg.mccy.docker;

import me.itzg.docker.types.State;

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

    public static ContainerStatus fromContainerInspect(State inspectedState) {
        if (Boolean.TRUE.equals(inspectedState.getRunning())) {
            return RUNNING;
        }
        else if (Boolean.TRUE.equals(inspectedState.getPaused())) {
            return PAUSED;
        }
        else if (Boolean.TRUE.equals(inspectedState.getRestarting())) {
            return RESTARTING;
        }
        else {
            return EXITED;
        }
    }
}
