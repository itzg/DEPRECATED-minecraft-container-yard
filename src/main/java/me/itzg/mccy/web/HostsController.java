package me.itzg.mccy.web;

import me.itzg.mccy.model.DockerHost;
import me.itzg.mccy.services.DatastoreService;
import org.springframework.beans.factory.annotation.Autowired;
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

    public static class SetBody {
        @NotNull
        public String address;

        public int port;
    }

    @Autowired
    private DatastoreService datastoreService;

    @RequestMapping(method = RequestMethod.GET)
    public Collection<DockerHost> getAll() {
        return datastoreService.getAll();
    }

    @RequestMapping(value = "/{name}", method = RequestMethod.PUT)
    public void set(@PathVariable String name, @RequestBody SetBody body) {
        final DockerHost dockerHost = new DockerHost();
        dockerHost.setName(name);
        dockerHost.setIpAddr(body.address);
        dockerHost.setTcpPort(body.port);

        datastoreService.set(dockerHost);
    }
}
