package me.itzg.mccy.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.itzg.mccy.docker.DockerClientService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * General configuration/bean setup for the services.
 *
 * @author Geoff Bourne
 * @since 3/8/2015
 */
@EnableCaching
@Configuration
public class ServicesConfig {

    @Bean @Qualifier("docker")
    public RestTemplate dockerRestTemplate() {
        return new RestTemplate();
    }

    @Bean @Qualifier("json")
    public RestTemplate jsonOnlyRestTemplate() {
        // force it to ignore MediaType
        final MappingJackson2HttpMessageConverter httpMessageConverter = new MappingJackson2HttpMessageConverter(objectMapper()) {
            @Override
            protected boolean canRead(MediaType mediaType) {
                return true;
            }
        };

        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        messageConverters.add(httpMessageConverter);

        return new RestTemplate(messageConverters);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public DockerClientService dockerClientService() {
        return new DockerClientService(dockerRestTemplate(), objectMapper());
    }

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("imageTracker");
    }

}
