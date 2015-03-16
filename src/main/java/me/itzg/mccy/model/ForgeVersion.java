package me.itzg.mccy.model;

/**
 * @author Geoff Bourne
 * @since 3/15/2015
 */
public class ForgeVersion {
    private String vanillaVersion;
    private String forgeVersion;
    private String type;

    public String getVanillaVersion() {
        return vanillaVersion;
    }

    public void setVanillaVersion(String vanillaVersion) {
        this.vanillaVersion = vanillaVersion;
    }

    public String getForgeVersion() {
        return forgeVersion;
    }

    public void setForgeVersion(String forgeVersion) {
        this.forgeVersion = forgeVersion;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
