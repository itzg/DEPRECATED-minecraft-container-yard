package me.itzg.mccy.services;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Geoff Bourne
 * @since 3/7/2015
 */
@Configuration
@ConfigurationProperties(prefix = "datastore")
public class DatastoreConfig {

    private String path;

    private String clusterName;

    @Bean
    public Node esNode() {
        final Settings esSettings = ImmutableSettings.settingsBuilder()
                .put("path.data", path)
                .build();

        return NodeBuilder.nodeBuilder()
                .settings(esSettings)
                .clusterName(clusterName)
                .data(true)
                .client(false)
                .build()
                .start(); // NEEDED TO AVOID "client only" mode
    }

    @Bean
    public Client esClient() {
        return esNode().client();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }
}
