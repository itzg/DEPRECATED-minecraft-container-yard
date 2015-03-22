package me.itzg.mccy.services;

import com.google.common.net.HostAndPort;
import me.itzg.docker.types.containers.ContainersResponse;
import me.itzg.docker.types.containers.create.ContainersCreateRequest;
import me.itzg.docker.types.containers.create.ContainersCreateResponse;
import me.itzg.docker.types.containers.create.ExposedPorts;
import me.itzg.docker.types.containers.create.ExposedPortsProperty;
import me.itzg.docker.types.containers.create.HostConfig;
import me.itzg.docker.types.containers.create.PortBindings;
import me.itzg.docker.types.containers.create.PortBindingsProperty;
import me.itzg.docker.types.containers.inspect.ContainersInspectResponse;
import me.itzg.docker.types.containers.inspect.Ports;
import me.itzg.docker.types.containers.inspect.PortsProperty;
import me.itzg.docker.types.containers.inspect.State;
import me.itzg.mccy.MccyClientException;
import me.itzg.mccy.MccyConstants;
import me.itzg.mccy.MccyException;
import me.itzg.mccy.MccyServerException;
import me.itzg.mccy.docker.Binding;
import me.itzg.mccy.docker.ContainerStatus;
import me.itzg.mccy.docker.ContainersOptions;
import me.itzg.mccy.docker.CreateContainerOptions;
import me.itzg.mccy.docker.DockerClientException;
import me.itzg.mccy.docker.DockerModelUtils;
import me.itzg.mccy.docker.ImageReference;
import me.itzg.mccy.model.CreateServerRequest;
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
import java.util.Collections;
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
        return HostAndPort.fromParts(dockerHost.getAddress(), dockerHost.getTcpPort());
    }

    public Collection<MinecraftServer> getServersOnHosts(Collection<DockerHost> dockerHosts, boolean withDetails, final boolean allContainers) throws MccyException {
        List<MinecraftServer> servers = new ArrayList<>();

        for (DockerHost dockerHost : dockerHosts) {
            final Collection<MinecraftServer> summaries = getServersOnHost(dockerHost, allContainers);
            if (!withDetails) {
                servers.addAll(summaries);
            }
            else {
                for (MinecraftServer summary : summaries) {
                    servers.add(getDetails(dockerHost, summary.getId()));
                }
            }
        }

        return servers;
    }

    public MinecraftServerDetails getDetails(DockerHost dockerHost, String serverId) throws MccyException {
        ContainersInspectResponse response = dockerClientService.inspectContainer(extractDockerHostAndPort(dockerHost), serverId);

        MinecraftServerDetails details = new MinecraftServerDetails();
        details.setHostAddress(dockerHost.getAddress());
        details.setDockerDaemonId(dockerHost.getDockerDaemonId());
        details.setId(response.getId());
        extractContainerStatus(response, details);
        details.setHostDataMount(extractHostDataMount(response));

        details.setContainerName(extractContainerName(response));
        details.setExposedPort(extractExposedPort(response));
        extractEnvVarConfiguration(response, details);

        return details;
    }

    public MinecraftServer createServer(DockerHost dockerHost, CreateServerRequest request) throws MccyException {

        List<String> env = new ArrayList<>();
        addToEnv(env, MccyConstants.ENV_EULA, "TRUE");
        addToEnv(env, MccyConstants.ENV_VERSION, request.getVersion());
        addToEnv(env, MccyConstants.ENV_TYPE, request.getType());
        // TODO: other vars

        final PortBindingsProperty hostPortBinding = new PortBindingsProperty()
                .withHostPort(Integer.toString(request.getPort()));

        PortBindings portBindings = new PortBindings()
                .withAdditionalProperty(MccyConstants.MC_SERVER_PORT_MAPPING, Collections.singletonList(hostPortBinding));

        HostConfig hostConfig = new HostConfig()
                .withPortBindings(portBindings);
        // TODO: if /data attached to host

        ExposedPorts exposedPorts = new ExposedPorts()
                .withAdditionalProperty(MccyConstants.MC_SERVER_PORT_MAPPING, new ExposedPortsProperty());
        final ContainersCreateRequest containersCreateRequest = new ContainersCreateRequest()
                .withImage(MccyConstants.MC_DOCKER_IMAGE)
                .withEnv(env)
                .withExposedPorts(exposedPorts)
                .withHostConfig(hostConfig)
                .withOpenStdin(true)
                .withTty(true);

        CreateContainerOptions containerOptions = new CreateContainerOptions()
                .withName(request.getName());


        try {
            ContainersCreateResponse response = dockerClientService.createContainer(extractDockerHostAndPort(dockerHost),
                    containersCreateRequest, containerOptions);

            if (response.getWarnings() != null && !response.getWarnings().isEmpty()) {
                throw new MccyClientException("Unable to create container: "+response.getWarnings());
            }

            dockerClientService.startContainer(extractDockerHostAndPort(dockerHost), response.getId());

            final MinecraftServer minecraftServer = new MinecraftServer();
            minecraftServer.setId(response.getId());
            minecraftServer.setDockerDaemonId(dockerHost.getDockerDaemonId());

            return minecraftServer;
        } catch (DockerClientException e) {
            throw e.isServerSideError() ? new MccyServerException(e) : new MccyClientException(e);
        }
    }

    private void addToEnv(List<String> env, String variable, String value) {
        if (value != null && !value.isEmpty()) {
            env.add(variable + "=" + value);
        }
    }

    private String extractHostDataMount(ContainersInspectResponse response) {
        final List<Binding> bindings = DockerModelUtils.parseHostConfigBinds(response.getHostConfig());
        for (Binding binding : bindings) {
            if (binding.getContainerPath().equals(MccyConstants.CONTAINER_DATA_PATH)) {
                return binding.getHostPath();
            }
        }
        return null;
    }

    private void extractContainerStatus(ContainersInspectResponse response, MinecraftServerDetails details) {
        final State state = response.getState();
        final ContainerStatus containerStatus = DockerClientService.fromContainerInspect(state);

        details.setContainerStatus(containerStatus);

        //TODO: after upgrading Jackson > 2.4.5 need to try the "format":"date-time" again

        switch (containerStatus) {
            case RUNNING:
                details.setLastStatusChange(state.getStartedAt());
                break;

            case EXITED:
                details.setLastStatusChange(state.getFinishedAt());
                break;
        }
    }

    private void extractEnvVarConfiguration(ContainersInspectResponse response, MinecraftServerDetails details) throws MccyException {
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

    private Integer extractExposedPort(ContainersInspectResponse response) {
        final Ports ports = response.getNetworkSettings().getPorts();
        if (ports != null) {
            final List<PortsProperty> portsProperties = ports.getAdditionalProperties().get(MccyConstants.MC_SERVER_PORT_MAPPING);
            if (portsProperties != null) {
                if (portsProperties.size() > 1) {
                    LOG.warn("More than one exposed port. Just using first one of {}", portsProperties);
                }
                final PortsProperty mapping = portsProperties.get(0);
                return Integer.parseInt(mapping.getHostPort());
            }
        }
        return null;
    }

    private String extractContainerName(ContainersInspectResponse response) {
        final String name = response.getName();
        if (name.startsWith("/")) {
            return name.substring(1, name.length());
        }
        else {
            return name;
        }
    }
}
