package me.itzg.mccy.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * General configuration/bean setup for the services.
 *
 * @author Geoff Bourne
 * @since 3/8/2015
 */
@Configuration
public class ServicesConfig {

    @Bean @Qualifier("docker")
    public RestTemplate dockerRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
