package me.itzg.mccy.model;

import me.itzg.mccy.docker.ContainerStatus;

import java.util.Date;
import java.util.List;

/**
 * Models full details about a Minecraft server container
 * @author Geoff Bourne
 * @since 3/14/2015
 */
public class MinecraftServerDetails extends MinecraftServer {

    private String containerName;

    private Integer exposedPort;

    private ContainerStatus containerStatus;

    private String lastStatusChange;

    private String requestedVersion;

    private String type;

    private String motd;

    private String levelName;

    private String configuredSeed;

    private String mode;

    private String[] originalOps;

    private String iconUrl;

    public String getRequestedVersion() {
        return requestedVersion;
    }

    @EnvVar("VERSION")
    public void setRequestedVersion(String requestedVersion) {
        this.requestedVersion = requestedVersion;
    }

    public String getType() {
        return type;
    }

    @EnvVar("TYPE")
    public void setType(String type) {
        this.type = type;
    }

    public String getMotd() {
        return motd;
    }

    @EnvVar("MOTD")
    public void setMotd(String motd) {
        this.motd = motd;
    }

    public String getLevelName() {
        return levelName;
    }

    @EnvVar("LEVEL")
    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public String getConfiguredSeed() {
        return configuredSeed;
    }

    @EnvVar("SEED")
    public void setConfiguredSeed(String configuredSeed) {
        this.configuredSeed = configuredSeed;
    }

    public String getMode() {
        return mode;
    }

    @EnvVar("MODE")
    public void setMode(String mode) {
        this.mode = mode;
    }

    public String[] getOriginalOps() {
        return originalOps;
    }

    @EnvVar("OPS")
    public void setOriginalOps(String[] originalOps) {
        this.originalOps = originalOps;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    @EnvVar("ICON")
    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public Integer getExposedPort() {
        return exposedPort;
    }

    public void setExposedPort(Integer exposedPort) {
        this.exposedPort = exposedPort;
    }

    public ContainerStatus getContainerStatus() {
        return containerStatus;
    }

    public void setContainerStatus(ContainerStatus containerStatus) {
        this.containerStatus = containerStatus;
    }

    public String getLastStatusChange() {
        return lastStatusChange;
    }

    public void setLastStatusChange(String lastStatusChange) {
        this.lastStatusChange = lastStatusChange;
    }
}
