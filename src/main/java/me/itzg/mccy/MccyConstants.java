package me.itzg.mccy;

import com.google.common.net.HostAndPort;

/**
 * @author Geoff Bourne
 * @since 3/13/2015
 */
public class MccyConstants {
    public static final int DEFAULT_DOCKER_PORT = 2375;

    public static HostAndPort resolveHostAddress(HostAndPort incoming) {
        return incoming.withDefaultPort(DEFAULT_DOCKER_PORT);
    }
}
