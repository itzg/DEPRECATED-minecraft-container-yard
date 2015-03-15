package me.itzg.mccy.model;

import org.springframework.core.style.ToStringCreator;

/**
 * @author Geoff Bourne
 * @since 3/7/2015
 */
public class DockerHost {
    public static final String TYPE = "docker-host";
    public static final String NAME = "name";

    private String name;
    private String ipAddr;
    private int tcpPort;
    private String dockerDaemonId;

    @Override
    public String toString() {
        return new ToStringCreator(this)
                .append("dockerDaemonId", dockerDaemonId)
                .append("name", name)
                .append("ipAddr", ipAddr)
                .append("tcpPort", tcpPort)
                .toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public void setTcpPort(int tcpPort) {
        this.tcpPort = tcpPort;
    }

    /**
     * <code>ID</code> field of <code>/info</code> REST resource
     * <pre>AD5T:EH4O:WAYH:DNWF:OAIM:6CUC:KB2P:6C6A:TNVY:N7Z4:QR5S:KK5N</pre>
     * @return
     */
    public String getDockerDaemonId() {
        return dockerDaemonId;
    }

    public void setDockerDaemonId(String dockerDaemonId) {
        this.dockerDaemonId = dockerDaemonId;
    }
}
