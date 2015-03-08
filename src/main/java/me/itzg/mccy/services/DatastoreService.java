package me.itzg.mccy.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.itzg.mccy.model.DockerHost;
import me.itzg.mccy.model.Index;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Geoff Bourne
 * @since 3/7/2015
 */
@Service
public class DatastoreService {
    private static Logger LOG = LoggerFactory.getLogger(DatastoreService.class);

    @Autowired
    private Client esClient;

    private ObjectMapper objectMapper = new ObjectMapper();

    public void set(DockerHost dockerHost) {
        try {
            final IndexResponse indexResponse = esClient.prepareIndex().setIndex(Index.NAME)
                    .setType(DockerHost.TYPE)
                    .setId(dockerHost.getName())
                    .setSource(objectMapper.writeValueAsBytes(dockerHost))
                    .get();
        } catch (JsonProcessingException e) {
            LOG.warn("Unable to encode", e);
        }
    }

    public Collection<DockerHost> getAll() {
        final SearchResponse searchResponse = esClient.prepareSearch()
                .setIndices(Index.NAME)
                .setTypes(DockerHost.TYPE)
                .get();

        final SearchHit[] hits = searchResponse.getHits().getHits();

        List<DockerHost> results = new ArrayList<>(hits.length);
        for (SearchHit hit : hits) {
            final byte[] source = hit.source();
            try {
                final DockerHost dockerHost = objectMapper.readValue(source, DockerHost.class);
                results.add(dockerHost);
            } catch (IOException e) {
                LOG.warn("Failed to parse", e);
            }
        }

        return results;
    }
}
