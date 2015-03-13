package me.itzg.mccy.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.itzg.mccy.MccyClientException;
import me.itzg.mccy.MccyException;
import me.itzg.mccy.MccyServerException;
import me.itzg.mccy.model.DockerHost;
import me.itzg.mccy.model.Index;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Wraps all the gory ES bits.
 *
 * @author Geoff Bourne
 * @since 3/7/2015
 */
@Service
public class DatastoreService {
    private static Logger LOG = LoggerFactory.getLogger(DatastoreService.class);

    @Autowired
    private Client esClient;

    @Autowired
    private ObjectMapper objectMapper;

    public void store(DockerHost dockerHost) {
        try {
            final IndexResponse indexResponse = esClient.prepareIndex().setIndex(Index.NAME)
                    .setType(DockerHost.TYPE)
                    .setId(dockerHost.getDockerDaemonId())
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

    public DockerHost getHostByIdOrName(String idOrName) throws MccyException {
        final SearchResponse response = esClient.prepareSearch(Index.NAME)
                .setTypes(DockerHost.TYPE)
                .setQuery(QueryBuilders.boolQuery()
                        .minimumShouldMatch("1")
                        .should(QueryBuilders.idsQuery(DockerHost.TYPE).ids(idOrName))
                        .should(QueryBuilders.termQuery(DockerHost.NAME, idOrName))
                )
                .get();

        final SearchHits hits = response.getHits();
        if (hits.totalHits() == 0) {
            return null;
        }

        try {
            return objectMapper.readValue(hits.hits()[0].source(), DockerHost.class);
        } catch (IOException e) {
            throw new MccyServerException(e);
        }
    }

        /**
         * Find a {@link me.itzg.mccy.model.DockerHost} by Docker daemon ID
         * @param id the Docker daemon ID
         * @return the existing host or null, if not found
         * @throws MccyException if there was an access issue
         */
    public DockerHost getHostById(String id) throws MccyException {
        final GetResponse response = esClient.prepareGet(Index.NAME, DockerHost.TYPE, id)
                .get();

        if (!response.isExists()) {
            return null;
        }

        try {
            return objectMapper.readValue(response.getSourceAsBytes(), DockerHost.class);
        } catch (IOException e) {
            throw new MccyServerException(e);
        }
    }

    /**
     * Find a {@link me.itzg.mccy.model.DockerHost} by name
     * @param name either the original name reported by the Docker daemon or the user provided name
     * @return the existing host or null, if not found
     * @throws MccyException if too many were found or access issue
     */
    public DockerHost getHostByName(String name) throws MccyException {
        final SearchResponse response = esClient.prepareSearch(Index.NAME)
                .setTypes(DockerHost.TYPE)
                .setQuery(QueryBuilders.filteredQuery(QueryBuilders.termQuery(DockerHost.NAME, name), null))
                .get();

        final SearchHits hits = response.getHits();
        if (hits.totalHits() == 0) {
            return null;
        }
        else if (hits.totalHits() > 1) {
            throw new MccyClientException("More than one matched, only expected one");
        }

        try {
            return objectMapper.readValue(hits.hits()[0].source(), DockerHost.class);
        } catch (IOException e) {
            throw new MccyServerException(e);
        }
    }

    public void deleteHost(String id) throws MccyClientException {
        final DeleteResponse deleteResponse = esClient.prepareDelete(Index.NAME, DockerHost.TYPE, id)
                .get();

        if (!deleteResponse.isFound()) {
            throw new MccyClientException("Unable to locate host to delete");
        }
    }
}
