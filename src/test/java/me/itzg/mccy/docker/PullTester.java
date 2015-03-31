package me.itzg.mccy.docker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HostAndPort;
import me.itzg.docker.types.images.ImagesCreateResponse;
import org.springframework.web.client.RestTemplate;

/**
 * @author Geoff Bourne
 * @since 3/29/2015
 */
public class PullTester {
    public static void main(String[] args) throws DockerClientException {
        if (args.length == 0) {
            System.err.println("Requires Docker host:port and then one or more image names");
            System.exit(1);
        }

        if (args.length < 2) {
            System.err.println("Requires one or more image names");
            System.exit(2);
        }

        final PullImageListener listener = new PullImageListener() {

            @Override
            public void handlePullImageProgress(ImagesCreateResponse progress) {
                System.out.println("PROGRESS: "+progress.toString());
            }

            @Override
            public void handlePullException(Exception e) {
                System.err.println("ERROR   :"+e);
            }

            @Override
            public void handlePullFinished(CreateImageOptions options) {
                System.out.println("FINISHED: "+options);
            }
        };

        final DockerClientService client = new DockerClientService(new RestTemplate(), new ObjectMapper());

        final HostAndPort hostAndPort = HostAndPort.fromString(args[0]);
        for (int i = 1; i < args.length; i++) {
            final String image = args[i];

            client.pullImage(hostAndPort, new CreateImageOptions(image), listener);
        }
    }

}
