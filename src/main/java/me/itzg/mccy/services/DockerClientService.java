package me.itzg.mccy.services;

import com.google.common.net.HostAndPort;
import me.itzg.docker.types.InfoResponse;
import me.itzg.mccy.MccyClientException;
import me.itzg.mccy.MccyConstants;
import me.itzg.mccy.MccyException;
import me.itzg.mccy.MccyServerException;
import me.itzg.mccy.model.DockerHost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.ConnectException;

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
            return catchRestClientException(e);
        }
    }

    private InfoResponse catchRestClientException(RestClientException e) throws MccyClientException, MccyServerException {
        if (e instanceof ResourceAccessException) {
            final ResourceAccessException rae = (ResourceAccessException) e;
            final Throwable realCause = rae.getMostSpecificCause();
            if (realCause instanceof ConnectException) {
                throw new MccyClientException("Invoking Docker client API", e);
            }
        }
        throw new MccyServerException(e);
    }
}
