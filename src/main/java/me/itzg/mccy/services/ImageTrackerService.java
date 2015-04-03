package me.itzg.mccy.services;

import com.google.common.base.Preconditions;
import com.google.common.net.HostAndPort;
import me.itzg.docker.types.images.ImagesInspectResponse;
import me.itzg.mccy.MccyConstants;
import me.itzg.mccy.docker.DockerClientException;
import me.itzg.mccy.docker.DockerClientService;
import me.itzg.mccy.docker.DockerModelUtils;
import me.itzg.mccy.docker.ImageReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Provides cached tracking of whether a given image ID/name is a minecraft-server image. It's not good enough
 * to just check for {@value me.itzg.mccy.MccyConstants#MC_DOCKER_IMAGE} since the host may have since done
 * a <code>docker pull</code>. At that point a minecraft server container is now running with an "un-tagged", revision
 * style image ID.
 * @author Geoff Bourne
 * @since 4/2/2015
 */
@Service
public class ImageTrackerService {
    private static Logger LOG = LoggerFactory.getLogger(ImageTrackerService.class);

    @Autowired
    private DockerClientService dockerClientService;

    @Cacheable("imageTracker")
    public boolean isMinecraftServerImage(HostAndPort dockerHost, String imageNameId) throws DockerClientException {
        LOG.debug("Checking image {} on {}", imageNameId, dockerHost);
        Preconditions.checkArgument(imageNameId != null && !imageNameId.isEmpty());

        final ImageReference imageReference = ImageReference.fromIdentifier(imageNameId);

        // super easy case first
        if (imageReference.getImage().equals(MccyConstants.MC_DOCKER_IMAGE)) {
            return true;
        }
        if (imageReference.hasTag()) {
            // some other named image
            return false;
        }

        final ImagesInspectResponse response = dockerClientService.inspectImage(dockerHost, imageNameId);

        final Map<String, String> env = DockerModelUtils.splitEnvVars(response.getConfig().getEnv());
        if (checkEnvValue(env, MccyConstants.ENV_MC_IMAGE, MccyConstants.VAL_YES)) {
            return true;
        }
        else if (checkEnvValue(env, MccyConstants.ENV_TYPE, MccyConstants.DEFAULT_TYPE) &&
                checkEnvValue(env, MccyConstants.ENV_VERSION, MccyConstants.DEFAULT_VERSION)) {
            return true;
        }

        return false;
    }

    private boolean checkEnvValue(Map<String, String> env, String key, String expectedValue) {
        final String value = env.get(key);
        return value != null && value.equals(expectedValue);
    }
}
