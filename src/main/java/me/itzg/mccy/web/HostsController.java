package me.itzg.mccy.web;

import com.google.common.net.HostAndPort;
import me.itzg.mccy.MccyClientException;
import me.itzg.mccy.MccyException;
import me.itzg.mccy.model.DockerHost;
import me.itzg.mccy.model.MinecraftServer;
import me.itzg.mccy.services.HostsService;
import me.itzg.mccy.services.MinecraftServersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

/**
 * @author Geoff Bourne
 * @since 3/7/2015
 */
@RestController
@RequestMapping("/hosts")
public class HostsController {

    @Autowired
    private HostsService hostsService;

    @Autowired
    private MinecraftServersService serversService;

    @RequestMapping(method = RequestMethod.GET)
    public Collection<DockerHost> getAll() {
        return hostsService.getAll();
    }

    @RequestMapping(method = RequestMethod.POST)
    public DockerHost create(@RequestParam("address") String hostAndPort) throws MccyException {
        return hostsService.create(HostAndPort.fromString(hostAndPort));
    }

    @RequestMapping(value = "/{idOrName}", method = RequestMethod.GET)
    public ResponseEntity<DockerHost> getHost(@PathVariable String idOrName) throws MccyException {
        final DockerHost dockerHost = hostsService.get(idOrName);
        if (dockerHost != null) {
            return new ResponseEntity<>(dockerHost, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/{idOrName}", method = RequestMethod.PUT)
    public void set(@PathVariable String idOrName, @RequestBody DockerHost dockerHost) throws MccyException {
        hostsService.update(idOrName, dockerHost);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<String> delete(@PathVariable String id) throws MccyClientException {
        try {
            hostsService.delete(id);
            return new ResponseEntity<String>(HttpStatus.OK);
        } catch (MccyClientException e) {
            return new ResponseEntity<>("No host with id "+id+". Make sure you provided the id and not the name.", HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/{idOrName}/servers")
    public ResponseEntity<Collection<MinecraftServer>> getServersOnHost(@PathVariable String idOrName) throws MccyException {
        final DockerHost dockerHost = hostsService.get(idOrName);
        if (dockerHost != null) {
            final Collection<MinecraftServer> serversOnHost = serversService.getServersOnHost(dockerHost, true);
            return new ResponseEntity<Collection<MinecraftServer>>(serversOnHost, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<Collection<MinecraftServer>>(HttpStatus.NOT_FOUND);
        }
    }
}
