package me.itzg.mccy.model;

import java.util.Date;
import java.util.List;

/**
 * This is the blob provided by {@value me.itzg.mccy.MccyConstants#VERSIONS_VANILLA_JSON}
 * @author Geoff Bourne
 * @since 3/15/2015
 */
public class VanillaVersionsBlob {
    public Latest latest;

    public List<Version> versions;

    public static class Latest {
        public String snapshot;
        public String release;
    }

    public static class Version {
        public String id;
        public Date time;
        public Date releaseTime;
        public String type;
    }
}
