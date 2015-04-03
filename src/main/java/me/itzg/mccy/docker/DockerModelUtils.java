package me.itzg.mccy.docker;

import me.itzg.docker.types.containers.inspect.HostConfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Geoff Bourne
 * @since 3/21/2015
 */
public class DockerModelUtils {
    public static List<Binding> parseHostConfigBinds(HostConfig hostConfig) {
        final List<String> binds = hostConfig.getBinds();
        if (binds == null || binds.isEmpty()) {
            return Collections.emptyList();
        }

        Binding[] bindings = new Binding[binds.size()];
        for (int i = 0; i < binds.size(); i++) {
            final String[] parts = binds.get(i).split(Binding.SEPARATOR);
            bindings[i] = new Binding(parts[0], parts[1]);
        }

        return Arrays.asList(bindings);
    }

    /**
     * Processes the raw environment name=value strings into a map of name to value
     * @param envList the raw environment variable content provided in a Docker API response
     * @return map of name to value
     */
    public static Map<String, String> splitEnvVars(List<String> envList) {
        Map<String, String> asMap = new HashMap<>();

        for (String envVarValue : envList) {
            final String[] parts = envVarValue.split("=", 2);
            asMap.put(parts[0], parts[1]);
        }
        return asMap;
    }
}
