package me.itzg.mccy.web;

import com.google.common.net.HostAndPort;
import me.itzg.mccy.MccyClientException;
import me.itzg.mccy.MccyException;
import me.itzg.mccy.model.DockerHost;
import me.itzg.mccy.services.HostsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
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

    @RequestMapping(method = RequestMethod.GET)
    public Collection<DockerHost> getAll() {
        return hostsService.getAll();
    }

    @RequestMapping(method = RequestMethod.POST)
    public DockerHost create(@RequestParam("address") String hostAndPort) throws MccyException {
        return hostsService.create(HostAndPort.fromString(hostAndPort));
    }

    @RequestMapping(value = "/{nameOrId}", method = RequestMethod.GET)
    public ResponseEntity<DockerHost> getHost(@PathVariable String nameOrId) throws MccyException {
        final DockerHost dockerHost = hostsService.get(nameOrId);
        if (dockerHost != null) {
            return new ResponseEntity<>(dockerHost, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/{nameOrId}", method = RequestMethod.PUT)
    public void set(@PathVariable String nameOrId, @RequestBody DockerHost dockerHost) throws MccyException {
        hostsService.update(nameOrId, dockerHost);
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

    @RequestMapping(value = "/{namedOrId}/servers")
    public Collection<DockerHost> getServersOnHost(@PathVariable String nameOrId) {
        //TODO
        return null;
    }
}
