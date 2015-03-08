package me.itzg.mccy.model;

/**
 * @author Geoff Bourne
 * @since 3/7/2015
 */
public class DockerHost {
    public static final String TYPE = "docker-host";
    private String name;
    private String ipAddr;
    private int tcpPort;

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
}
