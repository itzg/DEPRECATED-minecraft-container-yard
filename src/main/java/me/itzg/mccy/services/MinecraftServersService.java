package me.itzg.mccy.services;

import com.google.common.net.HostAndPort;
import com.google.common.util.concurrent.ListenableFutureTask;
import me.itzg.docker.types.ContainerInspectResponse;
import me.itzg.docker.types.ContainersResponse;
import me.itzg.docker.types.Ports;
import me.itzg.docker.types.PortsProperty;
import me.itzg.mccy.MccyClientException;
import me.itzg.mccy.MccyConstants;
import me.itzg.mccy.MccyException;
import me.itzg.mccy.MccyServerException;
import me.itzg.mccy.docker.ContainersOptions;
import me.itzg.mccy.docker.ImageReference;
import me.itzg.mccy.model.DockerHost;
import me.itzg.mccy.model.EnvVar;
import me.itzg.mccy.model.MinecraftServer;
import me.itzg.mccy.model.MinecraftServerDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages access and creation of Minecraft servers.
 * @author Geoff Bourne
 * @since 3/13/2015
 */
@Service
public class MinecraftServersService {
    private static Logger LOG = LoggerFactory.getLogger(MinecraftServersService.class);

    @Autowired
    private DockerClientService dockerClientService;

    public Collection<MinecraftServer> getServersOnHost(DockerHost dockerHost, boolean allContainers) {
        final ContainersOptions options = new ContainersOptions();
        options.setShowAll(allContainers);
        Collection<ContainersResponse> containers = dockerClientService.getContainers(
                extractDockerHostAndPort(dockerHost), options);

        List<MinecraftServer> servers = new ArrayList<>();

        for (ContainersResponse container : containers) {
            final ImageReference imageReference = ImageReference.fromIdentifier(container.getImage());

            if (imageReference.getImage().equals(MccyConstants.MC_DOCKER_IMAGE)) {
                final MinecraftServer minecraftServer = new MinecraftServer();
                minecraftServer.setId(container.getId());
                minecraftServer.setDockerDaemonId(dockerHost.getDockerDaemonId());
                servers.add(minecraftServer);
            }
        }

        return servers;
    }

    private static HostAndPort extractDockerHostAndPort(DockerHost dockerHost) {
        return HostAndPort.fromParts(dockerHost.getIpAddr(), dockerHost.getTcpPort());
    }

    public Collection<MinecraftServer> getServersOnHosts(Collection<DockerHost> dockerHosts, final boolean allContainers) {
        List<MinecraftServer> servers = new ArrayList<>();

        for (DockerHost dockerHost : dockerHosts) {
            servers.addAll(getServersOnHost(dockerHost, allContainers));
        }

        return servers;
    }

    public MinecraftServerDetails getDetails(DockerHost dockerHost, String serverId) throws MccyException {
        ContainerInspectResponse response = dockerClientService.inspectContainer(extractDockerHostAndPort(dockerHost), serverId);

        MinecraftServerDetails details = new MinecraftServerDetails();
        details.setDockerDaemonId(dockerHost.getDockerDaemonId());
        details.setId(response.getId());

        details.setContainerName(extractContainerName(response));
        details.setExposedPort(extractExposedPort(response));
        extractEnvVarConfiguration(response, details);

        return details;
    }

    private void extractEnvVarConfiguration(ContainerInspectResponse response, MinecraftServerDetails details) throws MccyException {
        final List<String> envList = response.getConfig().getEnv();
        final Map<String, String> envVars = splitEnvVars(envList);

        final Method[] methods = details.getClass().getMethods();
        for (Method method : methods) {
            final EnvVar envVarAnno = method.getAnnotation(EnvVar.class);
            if (envVarAnno != null) {
                final String asConfigured = envVars.get(envVarAnno.value());
                final Class<?>[] parameterTypes = method.getParameterTypes();
                if (asConfigured != null) {
                    if (parameterTypes.length == 1) {
                        if (parameterTypes[0].equals(String.class)) {
                            try {
                                method.invoke(details, asConfigured);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                throw new MccyServerException(String.format("Unable to invoke %s on %s", method, details));
                            }
                        }
                        else if (parameterTypes[0].equals(String[].class)) {
                            final String[] parts = asConfigured.split(MccyConstants.STRING_ARRAY_SEPARATOR);
                            try {
                                method.invoke(details, (Object)parts);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                throw new MccyServerException(String.format(
                                        "Unable to pass %s to invoke %s on %s", Arrays.toString(parts), method, details));
                            }
                        }
                        else {
                            LOG.warn("Setter method had wrong signature: {}", method);
                        }
                    }
                    else {
                        LOG.warn("Setter method had wrong signature: {}", method);
                    }
                }
            }
        }
    }

    private Map<String, String> splitEnvVars(List<String> envList) {
        Map<String, String> asMap = new HashMap<>();

        for (String envVarValue : envList) {
            final String[] parts = envVarValue.split("=", 2);
            asMap.put(parts[0], parts[1]);
        }
        return asMap;
    }

    private Integer extractExposedPort(ContainerInspectResponse response) {
        final Ports ports = response.getNetworkSettings().getPorts();
        final List<PortsProperty> portsProperties = ports.getAdditionalProperties().get(MccyConstants.MC_SERVER_PORT_MAPPING);
        if (portsProperties != null) {
            if (portsProperties.size() > 1) {
                LOG.warn("More than one exposed port. Just using first one of {}", portsProperties);
            }
            final PortsProperty mapping = portsProperties.get(0);
            return Integer.parseInt(mapping.getHostPort());
        }
        return null;
    }

    private String extractContainerName(ContainerInspectResponse response) {
        final String name = response.getName();
        if (name.startsWith("/")) {
            return name.substring(1, name.length());
        }
        else {
            return name;
        }
    }
}
