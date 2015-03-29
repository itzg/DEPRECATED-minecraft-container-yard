package me.itzg.mccy.docker;

import com.google.common.base.Preconditions;
import com.google.common.net.HostAndPort;
import me.itzg.docker.types.containers.ContainersResponse;
import me.itzg.docker.types.containers.create.ContainersCreateRequest;
import me.itzg.docker.types.containers.create.ContainersCreateResponse;
import me.itzg.docker.types.containers.inspect.ContainersInspectResponse;
import me.itzg.docker.types.InfoResponse;
import me.itzg.docker.types.containers.inspect.State;
import me.itzg.mccy.MccyClientException;
import me.itzg.mccy.MccyConstants;
import me.itzg.mccy.MccyException;
import me.itzg.mccy.MccyServerException;
import me.itzg.mccy.docker.ContainerStatus;
import me.itzg.mccy.docker.ContainersOptions;
import me.itzg.mccy.docker.CreateContainerOptions;
import me.itzg.mccy.docker.DockerClientException;
import me.itzg.utils.collections.MapBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.ConnectException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Wraps/abstract Docker client access around HTTP or some pre-rolled library
 *
 * @author Geoff Bourne
 * @since 3/8/2015
 */
public class DockerClientService {

    private RestTemplate restTemplate;

    @Autowired
    public DockerClientService(@Qualifier("docker") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public static ContainerStatus fromContainerInspect(State inspectedState) {
        if (Boolean.TRUE.equals(inspectedState.getRunning())) {
            return ContainerStatus.RUNNING;
        }
        else if (Boolean.TRUE.equals(inspectedState.getPaused())) {
            return ContainerStatus.PAUSED;
        }
        else if (Boolean.TRUE.equals(inspectedState.getRestarting())) {
            return ContainerStatus.RESTARTING;
        }
        else if (MccyConstants.ZERO_ZULU.equals(inspectedState.getFinishedAt())) {
            return ContainerStatus.CREATED;
        }
        else {
            return ContainerStatus.EXITED;
        }
    }

    public InfoResponse getInfo(HostAndPort dockerHost) throws MccyException {
        try {
            return restTemplate.getForObject("http://{host}:{port}/info", InfoResponse.class,
                    dockerHost.getHostText(), dockerHost.getPortOrDefault(MccyConstants.DEFAULT_DOCKER_PORT));
        } catch (RestClientException e) {
            catchRestClientException(e);
            return null;
        }
    }

    private void catchRestClientException(RestClientException e) throws MccyClientException, MccyServerException {
        if (e instanceof ResourceAccessException) {
            final ResourceAccessException rae = (ResourceAccessException) e;
            final Throwable realCause = rae.getMostSpecificCause();
            if (realCause instanceof ConnectException) {
                throw new MccyClientException("Invoking Docker client API", e);
            }
        }
        throw new MccyServerException(e);
    }

    public Collection<ContainersResponse> getContainers(HostAndPort dockerHost, ContainersOptions containersOptions) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(dockerHost.getHostText())
                .port(dockerHost.getPort())
                .path("/containers/json");

        uriBuilder.queryParam("all", containersOptions.isShowAll());
        if (containersOptions.getLimit() != null) {
            uriBuilder.queryParam("limit", containersOptions.getLimit());
        }
        if (containersOptions.getSinceId() != null) {
            uriBuilder.queryParam("since", containersOptions.getSinceId());
        }
        if (containersOptions.getBeforeId() != null) {
            uriBuilder.queryParam("before", containersOptions.getBeforeId());
        }
        if (containersOptions.isIncludeSize()) {
            uriBuilder.queryParam("size", "true");
        }
        if (containersOptions.getFilters() != null) {
            uriBuilder.queryParam("filters", containersOptions.getFilters());
        }
        if (containersOptions.getWithExitCode() != null) {
            uriBuilder.queryParam("exited", containersOptions.getWithExitCode());
        }
        if (containersOptions.getWithStatus() != null) {
            uriBuilder.queryParam("status", containersOptions.getWithStatus().getEncoded());
        }

        final ContainersResponse[] response = restTemplate.getForObject(uriBuilder.toUriString(), ContainersResponse[].class);

        return Arrays.asList(response);
    }

    public ContainersInspectResponse inspectContainer(HostAndPort dockerHost, String serverId) throws MccyClientException, MccyServerException {
        try {
            return restTemplate.getForObject("http://{host}:{port}/containers/{serverId}/json", ContainersInspectResponse.class,
                    dockerHost.getHostText(), dockerHost.getPortOrDefault(MccyConstants.DEFAULT_DOCKER_PORT),
                    serverId);
        } catch (RestClientException e) {
            catchRestClientException(e);
            return null;
        }
    }

    public ContainersCreateResponse createContainer(final HostAndPort dockerHostAndPort,
                                                    final ContainersCreateRequest request,
                                                    final CreateContainerOptions options) throws DockerClientException {

        final Map<String, String> variables = new HashMap<>();
        variables.put("host", dockerHostAndPort.getHostText());
        variables.put("port", Integer.toString(dockerHostAndPort.getPortOrDefault(MccyConstants.DEFAULT_DOCKER_PORT)));
        if (options.getName() != null) {
            variables.put("name", options.getName());
        }

        return wrapRestTemplateCall("Creating container", new Callable<ContainersCreateResponse>() {
            @Override
            public ContainersCreateResponse call() throws Exception {
                final URI uri = buildUri(dockerHostAndPort, "/containers/create", null,
                        "name", options.getName());

                return restTemplate.postForObject(uri, request,
                        ContainersCreateResponse.class);
            }
        });
    }

    private static URI buildUri(HostAndPort dockerHostAndPort, String pathTemplate, Map<String,?> pathVariables,
                                String... queryParams) {
        final UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(dockerHostAndPort.getHostText())
                .port(dockerHostAndPort.getPortOrDefault(MccyConstants.DEFAULT_DOCKER_PORT))
                .path(pathTemplate);

        for (int i = 0; i+1 < queryParams.length; i += 2) {
            uriComponentsBuilder.queryParam(queryParams[i], queryParams[i+1]);
        }

        final UriComponents built = uriComponentsBuilder.build();

        return (pathVariables != null ? built.expand(pathVariables) : built).toUri();
    }

    public void startContainer(HostAndPort dockerHostAndPort, String containerId) throws DockerClientException {
        Preconditions.checkArgument(containerId != null, "containerId is required");

        final Map<String, Object> variables = MapBuilder.<String,Object>startMap()
                .put("host", dockerHostAndPort.getHostText())
                .put("port", dockerHostAndPort.getPortOrDefault(dockerHostAndPort.getPort()))
                .put("containerId", containerId)
                .build();

        wrapRestTemplateCall("Starting container", new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                restTemplate.postForObject(
                        "http://{host}:{port}/containers/{containerId}/start", null, String.class, variables);
                return null;
            }
        });

    }

    private static <T> T wrapRestTemplateCall(String scenarioMessage, Callable<T> callable) throws DockerClientException {
        try {

            return callable.call();

        } catch (HttpClientErrorException e) {
            throw new DockerClientException(String.format("%s: %s", scenarioMessage, e.getResponseBodyAsString()), e);
        } catch (HttpServerErrorException e) {
            throw new DockerClientException(String.format("%s: %s", scenarioMessage, e.getResponseBodyAsString()), e)
                    .setServerSideError(true);
        } catch (Exception e) {
            // catch all
            throw new DockerClientException(e)
                    .setServerSideError(true);
        }
    }
}
