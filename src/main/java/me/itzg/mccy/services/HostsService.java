package me.itzg.mccy.services;

import com.google.common.net.HostAndPort;
import me.itzg.docker.types.InfoResponse;
import me.itzg.mccy.MccyClientException;
import me.itzg.mccy.MccyConstants;
import me.itzg.mccy.MccyException;
import me.itzg.mccy.model.DockerHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * Manages Docker (daemon) hosts.
 *
 * @author Geoff Bourne
 * @since 3/8/2015
 */
@Service
public class HostsService {
    private static Logger LOG = LoggerFactory.getLogger(HostsService.class);

    @Autowired
    private DatastoreService datastoreService;

    @Autowired
    private DockerClientService dockerClientService;

    public DockerHost set(String nameOrId, HostAndPort address) throws MccyException {
        final InfoResponse infoResponse = dockerClientService.getInfo(address);

        final DockerHost dockerHost = new DockerHost();
        if (dockerHost.getDockerDaemonId() == null) {
            dockerHost.setDockerDaemonId(infoResponse.getID());
        }
        datastoreService.store(dockerHost);

        return dockerHost;
    }

    public Collection<DockerHost> getAll() {
        return getAll(null);
    }

    /**
     * Get all hosts (or a specified subset of them)
     * @param hostIds if null, get all hosts, otherwise, specifies a subset of daemon IDs
     * @return
     */
    public Collection<DockerHost> getAll(List<String> hostIds) {
        return datastoreService.getAll(hostIds);
    }

    public DockerHost create(HostAndPort address) throws MccyException {
        final InfoResponse infoResponse = dockerClientService.getInfo(address);

        DockerHost host = datastoreService.getHostById(infoResponse.getID());
        if (host != null) {
            LOG.debug("Host at {} already existed by ID: {}", address, host);
            return host;
        }

        host = datastoreService.getHostByName(infoResponse.getName());
        if (host != null) {
            LOG.debug("Host at {} already existed by name: {}", address, host);
            return host;
        }

        // Didn't exist, create it
        host = new DockerHost();
        address = MccyConstants.resolveHostAddress(address);
        host.setIpAddr(address.getHostText());
        host.setTcpPort(address.getPort());
        host.setName(infoResponse.getName());
        host.setDockerDaemonId(infoResponse.getID());

        datastoreService.store(host);

        return host;
    }

    public DockerHost update(String nameOrId, DockerHost dockerHost) {
        //TODO
        return dockerHost;
    }

    public void delete(String id) throws MccyClientException {
        datastoreService.deleteHost(id);
    }

    public DockerHost get(String nameOrId) throws MccyException {
        return datastoreService.getHostByIdOrName(nameOrId);
    }
}
