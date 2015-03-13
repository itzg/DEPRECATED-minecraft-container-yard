package me.itzg.mccy.web;

import me.itzg.mccy.model.MinecraftServer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Geoff Bourne
 * @since 3/8/2015
 */
@RestController
@RequestMapping("/servers")
public class ServersController {

    @RequestMapping(method = RequestMethod.GET)
    public Collection<MinecraftServer> getAllServers() {
        List<MinecraftServer> servers = new ArrayList<>();

        return servers;
    }
}
