package dev.langchain4j.store.embedding.azure.cosmos.mongo.vcore.spring;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.azure.cosmos.mongo.vcore.AzureCosmosDbMongoVCoreEmbeddingStore;
import dev.langchain4j.store.embedding.spring.EmbeddingStoreAutoConfigurationIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.List;

import static dev.langchain4j.internal.Utils.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "AZURE_COSMOS_MONGO_VCORE_CONNECTION_STRING", matches = ".+")
class AzureCosmosDbMongoVCoreEmbeddingStoreAutoConfigurationIT extends EmbeddingStoreAutoConfigurationIT {

    private static final String CONNECTION_STRING = System.getenv("AZURE_COSMOS_MONGO_VCORE_CONNECTION_STRING");

    String collectionName;

    @BeforeEach
    void setCollectionName() {
        collectionName = "test_" + randomUUID().replace("-", "_");
    }

    @Override
    protected Class<?> autoConfigurationClass() {
        return AzureCosmosDbMongoVCoreEmbeddingStoreAutoConfiguration.class;
    }

    @Override
    protected Class<? extends EmbeddingStore<TextSegment>> embeddingStoreClass() {
        return AzureCosmosDbMongoVCoreEmbeddingStore.class;
    }

    @Override
    protected String[] properties() {
        return new String[]{
                "langchain4j.azure.cosmos-mongo-vcore.connection-string=" + CONNECTION_STRING,
                "langchain4j.azure.cosmos-mongo-vcore.database-name=testdb",
                "langchain4j.azure.cosmos-mongo-vcore.collection-name=" + collectionName,
                "langchain4j.azure.cosmos-mongo-vcore.index-name=testindex",
                "langchain4j.azure.cosmos-mongo-vcore.create-index=true",
                "langchain4j.azure.cosmos-mongo-vcore.kind=vector-ivf"
        };
    }

    @Override
    protected String dimensionPropertyKey() {
        return "langchain4j.azure.cosmos-mongo-vcore.dimensions";
    }

    @Test
    void should_not_create_embedding_store_when_disabled() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(AzureCosmosDbMongoVCoreEmbeddingStoreAutoConfiguration.class))
                .withPropertyValues(
                        "langchain4j.azure.cosmos-mongo-vcore.enabled=false",
                        "langchain4j.azure.cosmos-mongo-vcore.connection-string=" + CONNECTION_STRING,
                        "langchain4j.azure.cosmos-mongo-vcore.database-name=testdb",
                        "langchain4j.azure.cosmos-mongo-vcore.collection-name=" + collectionName
                )
                .run(context -> {
                    assertThat(context).doesNotHaveBean(AzureCosmosDbMongoVCoreEmbeddingStore.class);
                });
    }

    @Test
    void should_not_create_embedding_store_when_user_provides_own_bean() {
        EmbeddingModel embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();
        String userCollectionName = "user_" + randomUUID().replace("-", "_");

        AzureCosmosDbMongoVCoreEmbeddingStore customStore = AzureCosmosDbMongoVCoreEmbeddingStore.builder()
                .connectionString(CONNECTION_STRING)
                .databaseName("testdb")
                .collectionName(userCollectionName)
                .indexName("userindex")
                .createIndex(true)
                .kind("vector-ivf")
                .dimensions(embeddingModel.dimension())
                .build();

        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(AzureCosmosDbMongoVCoreEmbeddingStoreAutoConfiguration.class))
                .withBean(AzureCosmosDbMongoVCoreEmbeddingStore.class, () -> customStore)
                .withPropertyValues(
                        "langchain4j.azure.cosmos-mongo-vcore.connection-string=" + CONNECTION_STRING,
                        "langchain4j.azure.cosmos-mongo-vcore.database-name=testdb",
                        "langchain4j.azure.cosmos-mongo-vcore.collection-name=" + collectionName,
                        "langchain4j.azure.cosmos-mongo-vcore.index-name=testindex",
                        "langchain4j.azure.cosmos-mongo-vcore.create-index=true",
                        "langchain4j.azure.cosmos-mongo-vcore.dimensions=" + embeddingModel.dimension()
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureCosmosDbMongoVCoreEmbeddingStore.class);
                    assertThat(context.getBean(AzureCosmosDbMongoVCoreEmbeddingStore.class)).isSameAs(customStore);
                });
    }

    @Test
    void should_create_embedding_store_with_hnsw_index() {
        EmbeddingModel embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();
        String hnswCollectionName = "hnsw_" + randomUUID().replace("-", "_");

        contextRunner
                .withPropertyValues(
                        "langchain4j.azure.cosmos-mongo-vcore.connection-string=" + CONNECTION_STRING,
                        "langchain4j.azure.cosmos-mongo-vcore.database-name=testdb",
                        "langchain4j.azure.cosmos-mongo-vcore.collection-name=" + hnswCollectionName,
                        "langchain4j.azure.cosmos-mongo-vcore.index-name=hnswindex",
                        "langchain4j.azure.cosmos-mongo-vcore.create-index=true",
                        "langchain4j.azure.cosmos-mongo-vcore.kind=vector-hnsw",
                        "langchain4j.azure.cosmos-mongo-vcore.dimensions=" + embeddingModel.dimension(),
                        "langchain4j.azure.cosmos-mongo-vcore.m=16",
                        "langchain4j.azure.cosmos-mongo-vcore.ef-construction=64",
                        "langchain4j.azure.cosmos-mongo-vcore.ef-search=40"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureCosmosDbMongoVCoreEmbeddingStore.class);
                    EmbeddingStore<TextSegment> embeddingStore = context.getBean(AzureCosmosDbMongoVCoreEmbeddingStore.class);

                    TextSegment segment = TextSegment.from("test with hnsw index");
                    Embedding embedding = embeddingModel.embed(segment.text()).content();

                    String id = embeddingStore.add(embedding, segment);
                    assertThat(id).isNotBlank();

                    EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                            .queryEmbedding(embedding)
                            .maxResults(10)
                            .build();
                    List<EmbeddingMatch<TextSegment>> matches = embeddingStore.search(searchRequest).matches();
                    assertThat(matches).hasSize(1);
                    assertThat(matches.get(0).embedded()).isEqualTo(segment);
                });
    }

    @Test
    void should_create_embedding_store_with_ivf_index() {
        EmbeddingModel embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();
        String ivfCollectionName = "ivf_" + randomUUID().replace("-", "_");

        contextRunner
                .withPropertyValues(
                        "langchain4j.azure.cosmos-mongo-vcore.connection-string=" + CONNECTION_STRING,
                        "langchain4j.azure.cosmos-mongo-vcore.database-name=testdb",
                        "langchain4j.azure.cosmos-mongo-vcore.collection-name=" + ivfCollectionName,
                        "langchain4j.azure.cosmos-mongo-vcore.index-name=ivfindex",
                        "langchain4j.azure.cosmos-mongo-vcore.create-index=true",
                        "langchain4j.azure.cosmos-mongo-vcore.kind=vector-ivf",
                        "langchain4j.azure.cosmos-mongo-vcore.dimensions=" + embeddingModel.dimension(),
                        "langchain4j.azure.cosmos-mongo-vcore.num-lists=100"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureCosmosDbMongoVCoreEmbeddingStore.class);
                    EmbeddingStore<TextSegment> embeddingStore = context.getBean(AzureCosmosDbMongoVCoreEmbeddingStore.class);

                    TextSegment segment = TextSegment.from("test with ivf index");
                    Embedding embedding = embeddingModel.embed(segment.text()).content();

                    String id = embeddingStore.add(embedding, segment);
                    assertThat(id).isNotBlank();

                    EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                            .queryEmbedding(embedding)
                            .maxResults(10)
                            .build();
                    List<EmbeddingMatch<TextSegment>> matches = embeddingStore.search(searchRequest).matches();
                    assertThat(matches).hasSize(1);
                    assertThat(matches.get(0).embedded()).isEqualTo(segment);
                });
    }

    @Test
    void should_create_embedding_store_with_application_name() {
        EmbeddingModel embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();
        String appNameCollectionName = "app_" + randomUUID().replace("-", "_");

        contextRunner
                .withPropertyValues(
                        "langchain4j.azure.cosmos-mongo-vcore.connection-string=" + CONNECTION_STRING,
                        "langchain4j.azure.cosmos-mongo-vcore.database-name=testdb",
                        "langchain4j.azure.cosmos-mongo-vcore.collection-name=" + appNameCollectionName,
                        "langchain4j.azure.cosmos-mongo-vcore.index-name=appnameindex",
                        "langchain4j.azure.cosmos-mongo-vcore.create-index=true",
                        "langchain4j.azure.cosmos-mongo-vcore.dimensions=" + embeddingModel.dimension(),
                        "langchain4j.azure.cosmos-mongo-vcore.application-name=my-spring-app"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureCosmosDbMongoVCoreEmbeddingStore.class);
                    EmbeddingStore<TextSegment> embeddingStore = context.getBean(AzureCosmosDbMongoVCoreEmbeddingStore.class);

                    TextSegment segment = TextSegment.from("test with application name");
                    Embedding embedding = embeddingModel.embed(segment.text()).content();

                    String id = embeddingStore.add(embedding, segment);
                    assertThat(id).isNotBlank();
                });
    }
}
