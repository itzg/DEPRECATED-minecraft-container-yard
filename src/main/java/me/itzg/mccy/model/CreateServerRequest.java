package me.itzg.mccy.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.itzg.mccy.MccyConstants;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.net.URL;

/**
 * @author Geoff Bourne
 * @since 3/21/2015
 */
public class CreateServerRequest {
    @NotNull
    private String hostId;

    @NotNull @Pattern(regexp = "[a-zA-Z0-9_-]+")
    private String name;

    @Min(1)
    private int port;

    private String version = MccyConstants.DEFAULT_VERSION;

    private String type = MccyConstants.DEFAULT_TYPE;

    private String ops;

    private String motd;

    private boolean useHostDataMount;

    private Integer uid;

    private URL serverIcon;

    private String levelSeed;

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMotd() {
        return motd;
    }

    public void setMotd(String motd) {
        this.motd = motd;
    }

    public boolean isUseHostDataMount() {
        return useHostDataMount;
    }

    public void setUseHostDataMount(boolean useHostDataMount) {
        this.useHostDataMount = useHostDataMount;
    }

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public URL getServerIcon() {
        return serverIcon;
    }

    public void setServerIcon(URL serverIcon) {
        this.serverIcon = serverIcon;
    }

    public String getLevelSeed() {
        return levelSeed;
    }

    public void setLevelSeed(String levelSeed) {
        this.levelSeed = levelSeed;
    }

    public String getOps() {
        return ops;
    }

    /**
     * Sets the user that will be op'ed (or have admin rights)
     * @param ops comma separated list of Minecraft user names
     */
    public void setOps(String ops) {
        this.ops = ops;
    }
}
