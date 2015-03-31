package me.itzg.mccy.docker;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HostAndPort;
import me.itzg.docker.types.InfoResponse;
import me.itzg.docker.types.containers.ContainersResponse;
import me.itzg.docker.types.containers.create.ContainersCreateRequest;
import me.itzg.docker.types.containers.create.ContainersCreateResponse;
import me.itzg.docker.types.containers.inspect.ContainersInspectResponse;
import me.itzg.docker.types.containers.inspect.State;
import me.itzg.docker.types.images.ImagesCreateResponse;
import me.itzg.mccy.MccyClientException;
import me.itzg.mccy.MccyConstants;
import me.itzg.mccy.MccyException;
import me.itzg.mccy.MccyServerException;
import me.itzg.utils.collections.MapBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Wraps/abstract Docker client access around HTTP or some pre-rolled library
 *
 * @author Geoff Bourne
 * @since 3/8/2015
 */
public class DockerClientService {
    private static Logger LOG = LoggerFactory.getLogger(DockerClientService.class);

    private static final int MAX_PULL_CREATE_ATTEMPTS = 5;

    private RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public DockerClientService(@Qualifier("docker") RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
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

        int pullCreateAttempts = MAX_PULL_CREATE_ATTEMPTS;

        if (options.isPullLatestRequested()) {
            LOG.debug("Pulling latest requested");
            pullImage(dockerHostAndPort, new CreateImageOptions(request.getImage()), null);
        }

        while (pullCreateAttempts-- > 0) {
            try {
                return wrapRestTemplateCall("Creating container", new Callable<ContainersCreateResponse>() {
                    @Override
                    public ContainersCreateResponse call() throws Exception {
                        final URI uri = buildUri(dockerHostAndPort, "/containers/create", null,
                                "name", options.getName());

                        return restTemplate.postForObject(uri, request,
                                ContainersCreateResponse.class);
                    }
                });
            } catch (DockerClientException e) {
                final HttpClientErrorException httpException = (HttpClientErrorException) e.getCause();
                if (httpException.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                    LOG.debug("Image not found, so issuing a pull");
                    pullImage(dockerHostAndPort, new CreateImageOptions(request.getImage()), null);
                }
            }
        }

        throw new DockerClientException("Unable to pull image/create container after "+MAX_PULL_CREATE_ATTEMPTS+" tries");
    }

    public List<ImagesCreateResponse> pullImage(HostAndPort dockerHostAndPort, final CreateImageOptions options, PullImageListener listener) throws DockerClientException {
        checkArgument(options != null, "createImageOptions is required");
        checkArgument(options.getFromImage() != null, "fromImage option is required");

        final PullImageListener resolvedListener = listener != null ? listener : new PullImageListener() {
            @Override
            public void handlePullImageProgress(ImagesCreateResponse progress) {
                LOG.debug("Pull progressing: {}", progress);
            }

            @Override
            public void handlePullException(Exception e) {
                LOG.warn("Issue while pulling", e);
            }

            @Override
            public void handlePullFinished(CreateImageOptions options) {
                LOG.debug("Pull of {} is finished", options);
            }
        };

        final URI uri = buildUri(dockerHostAndPort, "/images/create", null,
                "fromImage", options.getFromImage(),
                "fromSrc", options.getFromSrc(),
                "repo", options.getRepo(),
                "tag", options.getTag(),
                "registry", options.getRegistry());

        objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);

        return wrapRestTemplateCall("Pulling image", new Callable<List<ImagesCreateResponse>>() {
            @Override
            public List<ImagesCreateResponse> call() throws Exception {

                return restTemplate.execute(uri.toURL().toString(), HttpMethod.POST, null, new ResponseExtractor<List<ImagesCreateResponse>>() {
                    @Override
                    public List<ImagesCreateResponse> extractData(ClientHttpResponse httpResponse) throws IOException {
                        List<ImagesCreateResponse> responses = new ArrayList<ImagesCreateResponse>();

                        try {
                            final InputStream in = httpResponse.getBody();
                            ImagesCreateResponse response;
                            while ((response = objectMapper.readValue(in, ImagesCreateResponse.class)) != null) {
                                    resolvedListener.handlePullImageProgress(response);

                            }
                        } catch (JsonMappingException e) {
                            if (e.getMessage().contains("end")) {
                                resolvedListener.handlePullFinished(options);
                            } else {
                                resolvedListener.handlePullException(e);
                            }
                        }

                        return responses;
                    }
                });

            }
        });

    }

    /**
     *
     * @param dockerHostAndPort
     * @param pathTemplate a path template that optionally contains variable placeholders
     * @param pathVariables if not null, specifies variables to expand within the <code>pathTemplate</code>
     * @param queryParams pairs of query parameter name, values where values of null will be skipped
     * @return
     */
    private static URI buildUri(HostAndPort dockerHostAndPort, String pathTemplate, Map<String,?> pathVariables,
                                String... queryParams) {
        final UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(dockerHostAndPort.getHostText())
                .port(dockerHostAndPort.getPortOrDefault(MccyConstants.DEFAULT_DOCKER_PORT))
                .path(pathTemplate);

        for (int i = 0; i+1 < queryParams.length; i += 2) {
            if (queryParams[i+1] != null) {
                uriComponentsBuilder.queryParam(queryParams[i], queryParams[i+1]);
            }
        }

        final UriComponents built = uriComponentsBuilder.build();

        return (pathVariables != null ? built.expand(pathVariables) : built).toUri();
    }

    public void startContainer(HostAndPort dockerHostAndPort, String containerId) throws DockerClientException {
        checkArgument(containerId != null, "containerId is required");

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
