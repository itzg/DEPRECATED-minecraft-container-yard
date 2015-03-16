package me.itzg.mccy.web;

import me.itzg.mccy.model.DistributionType;
import me.itzg.mccy.model.ForgeVersion;
import me.itzg.mccy.model.ReleaseType;
import me.itzg.mccy.services.VersionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

/**
 * @author Geoff Bourne
 * @since 3/15/2015
 */
@RestController
@RequestMapping("/versions")
public class VersionsController {

    @Autowired
    private VersionsService versionsService;

    @RequestMapping(value = "/vanilla")
    public Collection<String> getVersions(@RequestParam(value = "release", required = false, defaultValue = "STABLE") ReleaseType releaseType) {
        return versionsService.getVanillaVersions(releaseType);
    }

    @RequestMapping("/forge")
    public Collection<ForgeVersion> getForgeVersions(
            @RequestParam(value = "release", required = false, defaultValue = "STABLE") ReleaseType releaseType) {
        return versionsService.getForgeVersions(releaseType);
    }
}
