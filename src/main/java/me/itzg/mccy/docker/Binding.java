package me.itzg.mccy.docker;

/**
 * Represents a Docker container's volume attachment.
 *
 * @author Geoff Bourne
 * @since 3/21/2015
 */
public class Binding {
    public static final String SEPARATOR = ":";

    private String hostPath;
    private String containerPath;

    public Binding(String hostPath, String containerPath) {
        this.hostPath = hostPath;
        this.containerPath = containerPath;
    }

    public String getHostPath() {
        return hostPath;
    }

    public void setHostPath(String hostPath) {
        this.hostPath = hostPath;
    }

    public String getContainerPath() {
        return containerPath;
    }

    public void setContainerPath(String containerPath) {
        this.containerPath = containerPath;
    }
}
