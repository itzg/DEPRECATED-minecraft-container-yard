package me.itzg.mccy.model;

/**
 * Represents a summary of a Minecraft Server instance
 * @author Geoff Bourne
 * @since 3/8/2015
 */
public class MinecraftServer {
    private String id;
    private String dockerDaemonId;

    /**
     *
     * @return the container ID
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     *
     * @return the Docker daemon ID that is hosting this server/container
     */
    public String getDockerDaemonId() {
        return dockerDaemonId;
    }

    public void setDockerDaemonId(String dockerDaemonId) {
        this.dockerDaemonId = dockerDaemonId;
    }
}
