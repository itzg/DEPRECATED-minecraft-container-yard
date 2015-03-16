package me.itzg.mccy;

import com.google.common.net.HostAndPort;

/**
 * @author Geoff Bourne
 * @since 3/13/2015
 */
public class MccyConstants {
    public static final int DEFAULT_DOCKER_PORT = 2375;
    public static final String MC_DOCKER_IMAGE = "itzg/minecraft-server";
    /**
     * The key in NetworkSettings/Ports/{key} of a {@link me.itzg.docker.types.ContainerInspectResponse}
     */
    public static final String MC_SERVER_PORT_MAPPING = "25565/tcp";
    public static final java.lang.String STRING_ARRAY_SEPARATOR = ",";

    public static final String VERSIONS_VANILLA_JSON = "https://s3.amazonaws.com/Minecraft.Download/versions/versions.json";
    public static final String VERSIONS_FORGE_JSON = "http://files.minecraftforge.net/maven/net/minecraftforge/forge/promotions_slim.json";

    public static HostAndPort resolveHostAddress(HostAndPort incoming) {
        return incoming.withDefaultPort(DEFAULT_DOCKER_PORT);
    }
}
