package dev.langchain4j.store.embedding.elasticsearch.spring;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import dev.langchain4j.store.embedding.spring.EmbeddingStoreAutoConfigurationIT;
import lombok.SneakyThrows;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

import static dev.langchain4j.internal.Utils.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

class ElasticsearchEmbeddingStoreAutoConfigurationIT extends EmbeddingStoreAutoConfigurationIT {

    static ElasticsearchTestContainerHelper elasticsearchTestContainerHelper = new ElasticsearchTestContainerHelper();

    String indexName;

    @BeforeAll
    static void startServices() throws IOException {
        elasticsearchTestContainerHelper.startServices();
        assertThat(elasticsearchTestContainerHelper.restClient).isNotNull();
    }

    @AfterAll
    static void stopServices() throws IOException {
        elasticsearchTestContainerHelper.stopServices();
    }

    @BeforeEach
    void setIndexName() {
        indexName = randomUUID();
    }

    @Override
    protected Class<?> autoConfigurationClass() {
        return ElasticsearchEmbeddingStoreAutoConfiguration.class;
    }

    @Override
    protected Class<? extends EmbeddingStore<TextSegment>> embeddingStoreClass() {
        return ElasticsearchEmbeddingStore.class;
    }

    @Override
    protected String[] properties() {
        return new String[]{
                "langchain4j.elasticsearch.serverUrl=https://" + elasticsearchTestContainerHelper.elasticsearch.getHttpHostAddress(),
                "langchain4j.elasticsearch.password=changeme",
                "langchain4j.elasticsearch.indexName=" + indexName,
                "langchain4j.elasticsearch.caCertificateAsBase64String=" + elasticsearchTestContainerHelper.certAsBase64
        };
    }

    @Override
    protected String dimensionPropertyKey() {
        return "langchain4j.elasticsearch.dimension";
    }

    @Override
    @SneakyThrows
    protected void awaitUntilPersisted(ApplicationContext context) {
        RestClient restClient = context.getBean(RestClient.class);
        restClient.performRequest(new Request("POST", "/" + indexName + "/_refresh"));
    }
}
