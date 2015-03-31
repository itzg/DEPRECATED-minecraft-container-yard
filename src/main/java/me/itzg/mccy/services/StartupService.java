package me.itzg.mccy.services;

import com.google.common.net.HostAndPort;
import me.itzg.mccy.MccyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Geoff Bourne
 * @since 3/15/2015
 */
@Service
public class StartupService implements CommandLineRunner {
    private static Logger LOG = LoggerFactory.getLogger(StartupService.class);

    @Autowired
    private HostsService hostsService;

    @Override
    public void run(String... args) throws Exception {
        List<MccyException> issues = new ArrayList<>();

        for (String arg : args) {
            if (!arg.startsWith("--")) {
                try {
                    hostsService.create(HostAndPort.fromString(arg));
                } catch (MccyException e) {
                    issues.add(e);
                }
            }
        }

        if (!issues.isEmpty()) {
            LOG.error("==============================================================");
            LOG.error("Invalid Docker host specs");
            LOG.error("");
            LOG.error("HINT: make sure you added a TCP socket listener to your Docker");
            LOG.error("      daemon configuration, such as -H tcp://0.0.0.0:2375");
            LOG.error("");
            for (MccyException issue : issues) {
                LOG.error("   {}", issue.getMessage());
            }
            LOG.error("==============================================================");
            throw new IllegalArgumentException("One or more invalid Docker host specs");
        }
    }
}
