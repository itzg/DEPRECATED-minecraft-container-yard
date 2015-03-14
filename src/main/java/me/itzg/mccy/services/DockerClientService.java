package me.itzg.mccy.services;

import com.google.common.net.HostAndPort;
import me.itzg.docker.types.ContainerInspectResponse;
import me.itzg.docker.types.ContainersResponse;
import me.itzg.docker.types.InfoResponse;
import me.itzg.mccy.MccyClientException;
import me.itzg.mccy.MccyConstants;
import me.itzg.mccy.MccyException;
import me.itzg.mccy.MccyServerException;
import me.itzg.mccy.docker.ContainersOptions;
import me.itzg.mccy.model.DockerHost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.ConnectException;
import java.util.Arrays;
import java.util.Collection;

/**
 * Wraps/abstract Docker client access around HTTP or some pre-rolled library
 *
 * @author Geoff Bourne
 * @since 3/8/2015
 */
@Service
public class DockerClientService {

    @Autowired
    @Qualifier("docker")
    private RestTemplate restTemplate;

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

    public ContainerInspectResponse inspectContainer(HostAndPort dockerHost, String serverId) throws MccyClientException, MccyServerException {
        try {
            return restTemplate.getForObject("http://{host}:{port}/containers/{serverId}/json", ContainerInspectResponse.class,
                    dockerHost.getHostText(), dockerHost.getPortOrDefault(MccyConstants.DEFAULT_DOCKER_PORT),
                    serverId);
        } catch (RestClientException e) {
            catchRestClientException(e);
            return null;
        }
    }
}
