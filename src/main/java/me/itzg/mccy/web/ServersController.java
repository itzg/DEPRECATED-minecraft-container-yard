package me.itzg.mccy.web;

import me.itzg.mccy.MccyClientException;
import me.itzg.mccy.MccyException;
import me.itzg.mccy.MccyServerException;
import me.itzg.mccy.model.DockerHost;
import me.itzg.mccy.model.MinecraftServer;
import me.itzg.mccy.model.MinecraftServerDetails;
import me.itzg.mccy.services.HostsService;
import me.itzg.mccy.services.MinecraftServersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Geoff Bourne
 * @since 3/8/2015
 */
@RestController
@RequestMapping("/servers")
public class ServersController {

    @Autowired
    private HostsService hostsService;

    @Autowired
    private MinecraftServersService serversService;

    @RequestMapping(method = RequestMethod.GET)
    public Collection<MinecraftServer> getServers(@RequestParam(value="host", required = false) List<String> hostIds,
                                                  @RequestParam(value="details", required = false, defaultValue = "false") boolean withDetails) throws MccyException {
        final Collection<DockerHost> hosts = hostsService.getAll(hostIds);

        return serversService.getServersOnHosts(hosts, withDetails, true);
    }

    @RequestMapping(value="/{serverId}", method = RequestMethod.GET)
    public MinecraftServerDetails getDetails(@PathVariable String serverId, @RequestParam("host") String hostId) throws MccyException {
        final Collection<DockerHost> dockerHosts = hostsService.getAll(Collections.singletonList(hostId));

        final DockerHost dockerHost = dockerHosts.iterator().next();

        return serversService.getDetails(dockerHost, serverId);
    }
}
