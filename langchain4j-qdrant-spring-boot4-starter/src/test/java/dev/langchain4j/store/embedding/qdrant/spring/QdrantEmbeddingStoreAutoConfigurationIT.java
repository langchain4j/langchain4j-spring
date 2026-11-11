package dev.langchain4j.store.embedding.qdrant.spring;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import dev.langchain4j.store.embedding.spring.EmbeddingStoreAutoConfigurationIT;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.VectorParams;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.qdrant.QdrantContainer;

import java.util.concurrent.ExecutionException;

import static dev.langchain4j.internal.Utils.randomUUID;

class QdrantEmbeddingStoreAutoConfigurationIT extends EmbeddingStoreAutoConfigurationIT {

    static QdrantContainer qdrant = new QdrantContainer("qdrant/qdrant:latest");

    static QdrantClient adminClient;

    String collectionName;

    @BeforeAll
    static void beforeAll() {
        qdrant.start();
        adminClient = new QdrantClient(
                QdrantGrpcClient.newBuilder(qdrant.getHost(), qdrant.getGrpcPort(), false).build());
    }

    @AfterAll
    static void afterAll() {
        adminClient.close();
        qdrant.stop();
    }

    @BeforeEach
    void createCollection() throws ExecutionException, InterruptedException {
        collectionName = "langchain4j_" + randomUUID().replace("-", "_");
        int dimension = new AllMiniLmL6V2QuantizedEmbeddingModel().dimension();
        adminClient.createCollectionAsync(
                collectionName,
                VectorParams.newBuilder()
                        .setDistance(Distance.Cosine)
                        .setSize(dimension)
                        .build()
        ).get();
    }

    @AfterEach
    void deleteCollection() throws ExecutionException, InterruptedException {
        adminClient.deleteCollectionAsync(collectionName).get();
    }

    @Override
    protected Class<?> autoConfigurationClass() {
        return QdrantEmbeddingStoreAutoConfiguration.class;
    }

    @Override
    protected Class<? extends EmbeddingStore<TextSegment>> embeddingStoreClass() {
        return QdrantEmbeddingStore.class;
    }

    @Override
    protected String[] properties() {
        return new String[]{
                "langchain4j.qdrant.host=" + qdrant.getHost(),
                "langchain4j.qdrant.port=" + qdrant.getGrpcPort(),
                "langchain4j.qdrant.collectionName=" + collectionName
        };
    }

    @Override
    protected String dimensionPropertyKey() {
        return "langchain4j.qdrant.dimension";  // declared in properties, not used by auto-config
    }

}
