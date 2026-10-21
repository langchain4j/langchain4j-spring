package dev.langchain4j.store.embedding.azure.documentdb.spring;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoDriverInformation;
import com.mongodb.ServerAddress;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.ListCollectionNamesIterable;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.azure.documentdb.AzureDocumentDbEmbeddingStore;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static dev.langchain4j.store.embedding.azure.documentdb.spring.AzureDocumentDbEmbeddingStoreProperties.PREFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AzureDocumentDbEmbeddingStoreAutoConfigurationTest {

    private static final String CONNECTION_STRING = "mongodb://documentdb.invalid:27017/?appName=from-uri";
    private static final String[] PROPERTIES = {
            PREFIX + ".connection-string=" + CONNECTION_STRING,
            PREFIX + ".database-name=database",
            PREFIX + ".collection-name=embeddings",
            PREFIX + ".kind=vector-ivf"
    };

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AzureDocumentDbEmbeddingStoreAutoConfiguration.class));
    private final ApplicationContextRunner configuredRunner = contextRunner.withPropertyValues(PROPERTIES);

    @Mock
    private MongoClient mongoClient;
    @Mock
    private MongoClient otherClient;
    @Mock
    private MongoDatabase database;
    @Mock
    private MongoCollection<?> collection;
    @Mock
    private ListCollectionNamesIterable collectionNames;
    @Mock
    private ListIndexesIterable<Document> indexes;
    @Mock
    private AggregateIterable<BsonDocument> results;
    @Mock
    private EmbeddingModel embeddingModel;
    @Mock
    private EmbeddingModel otherModel;
    @Captor
    private ArgumentCaptor<List<Bson>> pipeline;

    private MockedStatic<MongoClients> clients;

    @BeforeEach
    void intercept_client_creation() {
        clients = mockStatic(MongoClients.class);
    }

    @AfterEach
    void release_static_mock() {
        clients.close();
    }

    @ParameterizedTest
    @ValueSource(strings = {"vector-ivf", "vector-hnsw"})
    void should_bind_index_and_search_options(String kind) {
        ownedClient();
        missingIndex();
        emptySearchResults();

        configuredRunner.withPropertyValues(
                        PREFIX + ".enabled=true",
                        PREFIX + ".kind=" + kind,
                        PREFIX + ".create-index=true",
                        PREFIX + ".dimensions=3",
                        PREFIX + ".index-name=custom-index",
                        PREFIX + ".application-name=custom-application",
                        PREFIX + ".num-lists=100",
                        PREFIX + ".m=24",
                        PREFIX + ".ef-construction=128",
                        PREFIX + ".ef-search=80")
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureDocumentDbEmbeddingStore.class);
                    BsonDocument index = createdIndex();
                    assertThat(index.getString("name").getValue()).isEqualTo("custom-index");
                    BsonDocument options = index.getDocument("cosmosSearchOptions");
                    assertThat(options.getString("kind").getValue()).isEqualTo(kind);
                    assertThat(options.getInt32("dimensions").getValue()).isEqualTo(3);
                    assertThat(options.getString("similarity").getValue()).isEqualTo("COS");
                    BsonDocument search = search(context.getBean(AzureDocumentDbEmbeddingStore.class));
                    if (kind.equals("vector-ivf")) {
                        assertThat(options.getInt32("numLists").getValue()).isEqualTo(100);
                        assertThat(options).doesNotContainKeys("m", "efConstruction");
                        assertThat(search.getBoolean("returnStoredSource").getValue()).isTrue();
                        assertThat(search.getDocument("cosmosSearch")).doesNotContainKey("efSearch");
                    } else {
                        assertThat(options.getInt32("m").getValue()).isEqualTo(24);
                        assertThat(options.getInt32("efConstruction").getValue()).isEqualTo(128);
                        assertThat(options).doesNotContainKey("numLists");
                        assertThat(search.getDocument("cosmosSearch").getInt32("efSearch").getValue())
                                .isEqualTo(80);
                    }
                });

        MongoClientSettings settings = createdSettings();
        assertThat(settings.getApplicationName()).isEqualTo("custom-application");
        assertThat(settings.getClusterSettings().getHosts())
                .containsExactly(new ServerAddress("documentdb.invalid", 27017));
        verify(mongoClient).close();
    }

    @ParameterizedTest
    @ValueSource(strings = {"vector-ivf", "vector-hnsw"})
    void should_preserve_core_defaults(String kind) {
        ownedClient();
        missingIndex();
        emptySearchResults();

        configuredRunner.withPropertyValues(
                        PREFIX + ".kind=" + kind,
                        PREFIX + ".create-index=true",
                        PREFIX + ".dimensions=3")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    BsonDocument index = createdIndex();
                    assertThat(index.getString("name").getValue()).isEqualTo("defaultIndexAzureCosmos");
                    BsonDocument options = index.getDocument("cosmosSearchOptions");
                    BsonDocument search = search(context.getBean(AzureDocumentDbEmbeddingStore.class));
                    if (kind.equals("vector-ivf")) {
                        assertThat(options.getInt32("numLists").getValue()).isEqualTo(1);
                    } else {
                        assertThat(options.getInt32("m").getValue()).isEqualTo(16);
                        assertThat(options.getInt32("efConstruction").getValue()).isEqualTo(64);
                        assertThat(search.getDocument("cosmosSearch").getInt32("efSearch").getValue())
                                .isEqualTo(40);
                    }
                });

        assertThat(createdSettings().getApplicationName()).isEqualTo("LangChain4j");
    }

    @Test
    void should_not_create_store_when_disabled() {
        contextRunner.withPropertyValues(PREFIX + ".enabled=false").run(context -> {
            assertThat(context).hasNotFailed().doesNotHaveBean(AzureDocumentDbEmbeddingStore.class);
        });
        clients.verifyNoInteractions();
    }

    @ParameterizedTest
    @ValueSource(classes = {AzureDocumentDbEmbeddingStore.class, MongoClient.class})
    void should_back_off_when_required_classes_are_missing(Class<?> missingClass) {
        contextRunner.withClassLoader(new FilteredClassLoader(missingClass)).run(context -> {
            assertThat(context).hasNotFailed().doesNotHaveBean(AzureDocumentDbEmbeddingStore.class);
        });
        clients.verifyNoInteractions();
    }

    @ParameterizedTest
    @CsvSource(quoteCharacter = '"', value = {
            "connection-string, 'langchain4j.azure.documentdb.connection-string'",
            "database-name, 'langchain4j.azure.documentdb.database-name' must be set",
            "collection-name, 'langchain4j.azure.documentdb.collection-name' must be set",
            "kind, 'langchain4j.azure.documentdb.kind' must be set"
    })
    void should_fail_when_required_properties_are_missing(String property, String message) {
        assertFailure(withoutProperty(property), IllegalArgumentException.class, message);
    }

    @ParameterizedTest
    @ValueSource(strings = {"database-name", "collection-name", "kind"})
    void should_fail_when_required_properties_are_blank(String property) {
        assertFailure(withoutProperty(property).withPropertyValues(PREFIX + "." + property + "= "),
                IllegalArgumentException.class, "'" + PREFIX + "." + property + "' must be set");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ivf", "VECTOR_HNSW", "vector-diskann"})
    void should_reject_unsupported_kind(String kind) {
        assertFailure(configuredRunner.withPropertyValues(PREFIX + ".kind=" + kind),
                IllegalArgumentException.class, "This vector index type is not supported");
    }

    @ParameterizedTest
    @ValueSource(strings = {"dimensions", "num-lists", "m", "ef-construction", "ef-search"})
    void should_reject_invalid_numeric_properties(String property) {
        assertFailure(configuredRunner.withPropertyValues(PREFIX + "." + property + "=not-a-number"),
                NumberFormatException.class, property);
    }

    @Test
    void should_not_resolve_models_or_create_index_by_default() {
        ownedClient();
        configuredRunner.withBean("first", EmbeddingModel.class, () -> embeddingModel)
                .withBean("second", EmbeddingModel.class, () -> otherModel)
                .run(context -> assertThat(context).hasSingleBean(AzureDocumentDbEmbeddingStore.class));
        verifyNoInteractions(embeddingModel, otherModel);
        verify(collection, never()).listIndexes();
        verify(database, never()).runCommand(any(Bson.class));
    }

    @Test
    void should_infer_dimensions_from_embedding_model() {
        ownedClient();
        missingIndex();
        when(embeddingModel.dimension()).thenReturn(3);
        configuredRunner.withPropertyValues(PREFIX + ".create-index=true")
                .withBean(EmbeddingModel.class, () -> embeddingModel)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(createdIndex().getDocument("cosmosSearchOptions").getInt32("dimensions").getValue())
                            .isEqualTo(3);
                });
        verify(embeddingModel).dimension();
    }

    @Test
    void should_prefer_explicit_dimensions_without_resolving_models() {
        ownedClient();
        missingIndex();
        configuredRunner.withPropertyValues(PREFIX + ".create-index=true", PREFIX + ".dimensions=3")
                .withBean("first", EmbeddingModel.class, () -> embeddingModel)
                .withBean("second", EmbeddingModel.class, () -> otherModel)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(createdIndex().getDocument("cosmosSearchOptions").getInt32("dimensions").getValue())
                            .isEqualTo(3);
                });
        verifyNoInteractions(embeddingModel, otherModel);
    }

    @Test
    void should_use_primary_embedding_model() {
        ownedClient();
        missingIndex();
        when(embeddingModel.dimension()).thenReturn(3);
        configuredRunner.withPropertyValues(PREFIX + ".create-index=true")
                .withBean("first", EmbeddingModel.class, () -> embeddingModel, definition -> definition.setPrimary(true))
                .withBean("second", EmbeddingModel.class, () -> otherModel)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(createdIndex().getDocument("cosmosSearchOptions").getInt32("dimensions").getValue())
                            .isEqualTo(3);
                });
        verifyNoInteractions(otherModel);
    }

    @Test
    void should_reject_ambiguous_embedding_models() {
        assertFailure(configuredRunner.withPropertyValues(PREFIX + ".create-index=true")
                        .withBean("first", EmbeddingModel.class, () -> embeddingModel)
                        .withBean("second", EmbeddingModel.class, () -> otherModel),
                NoUniqueBeanDefinitionException.class, "EmbeddingModel");
    }

    @Test
    void should_require_dimensions_when_creating_index() {
        assertFailure(configuredRunner.withPropertyValues(PREFIX + ".create-index=true"),
                IllegalArgumentException.class, PREFIX + ".dimensions");
    }

    @ParameterizedTest
    @CsvSource({"true, 0", "true, -1", "false, 0", "false, -1"})
    void should_reject_non_positive_dimensions(boolean createIndex, int dimensions) {
        assertFailure(configuredRunner.withPropertyValues(
                        PREFIX + ".create-index=" + createIndex, PREFIX + ".dimensions=" + dimensions),
                IllegalArgumentException.class, "dimensions");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void should_reject_non_positive_model_dimensions(int dimensions) {
        when(embeddingModel.dimension()).thenReturn(dimensions);
        assertFailure(configuredRunner.withPropertyValues(PREFIX + ".create-index=true")
                        .withBean(EmbeddingModel.class, () -> embeddingModel),
                IllegalArgumentException.class, "dimensions");
    }

    @Test
    void should_propagate_dimension_inference_failure() {
        when(embeddingModel.dimension()).thenThrow(new UnsupportedOperationException("dimension is unavailable"));
        assertFailure(configuredRunner.withPropertyValues(PREFIX + ".create-index=true")
                        .withBean(EmbeddingModel.class, () -> embeddingModel),
                UnsupportedOperationException.class, "dimension is unavailable");
    }

    @Test
    void should_back_off_for_a_user_store_without_requiring_properties() {
        existingCollection();
        AzureDocumentDbEmbeddingStore store = AzureDocumentDbEmbeddingStore.builder()
                .mongoClient(mongoClient)
                .databaseName("database")
                .collectionName("embeddings")
                .kind("vector-ivf")
                .build();

        contextRunner.withBean(AzureDocumentDbEmbeddingStore.class, () -> store)
                .withBean("first", MongoClient.class, () -> mongoClient, definition -> definition.setDestroyMethodName(""))
                .withBean("second", MongoClient.class, () -> otherClient, definition -> definition.setDestroyMethodName(""))
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureDocumentDbEmbeddingStore.class);
                    assertThat(context.getBean(AzureDocumentDbEmbeddingStore.class)).isSameAs(store);
                });
        clients.verifyNoInteractions();
        verify(mongoClient, never()).close();
        verifyNoInteractions(otherClient);
    }

    @Test
    void should_use_caller_client_without_owning_it() {
        existingCollection();
        withoutProperty("connection-string")
                .withBean(MongoClient.class, () -> mongoClient, definition -> definition.setDestroyMethodName(""))
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureDocumentDbEmbeddingStore.class);
                    context.getBean(AzureDocumentDbEmbeddingStore.class).close();
                });
        clients.verifyNoInteractions();
        verify(mongoClient, never()).close();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void should_use_mongo_client_bean_when_connection_string_is_blank(String connectionString) {
        existingCollection();
        withoutProperty("connection-string")
                .withPropertyValues(PREFIX + ".connection-string=" + connectionString)
                .withBean(MongoClient.class, () -> mongoClient, definition -> definition.setDestroyMethodName(""))
                .run(context -> assertThat(context).hasSingleBean(AzureDocumentDbEmbeddingStore.class));
        clients.verifyNoInteractions();
        verify(mongoClient, never()).close();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void should_register_the_store_under_the_same_bean_name_on_both_client_paths(boolean withMongoClientBean) {
        ApplicationContextRunner runner;
        if (withMongoClientBean) {
            existingCollection();
            runner = withoutProperty("connection-string")
                    .withBean(MongoClient.class, () -> mongoClient, definition -> definition.setDestroyMethodName(""));
        } else {
            ownedClient();
            runner = configuredRunner;
        }
        runner.run(context -> assertThat(context)
                .hasSingleBean(AzureDocumentDbEmbeddingStore.class)
                .hasBean("azureDocumentDbEmbeddingStore"));
    }

    @Test
    void should_prefer_connection_string_over_a_mongo_client_bean() {
        ownedClient();
        configuredRunner.withBean(MongoClient.class, () -> otherClient, definition -> definition.setDestroyMethodName(""))
                .run(context -> assertThat(context).hasSingleBean(AzureDocumentDbEmbeddingStore.class));
        assertThat(createdSettings().getClusterSettings().getHosts())
                .containsExactly(new ServerAddress("documentdb.invalid", 27017));
        verify(mongoClient).close();
        verifyNoInteractions(otherClient);
    }

    @Test
    void should_use_primary_mongo_client() {
        existingCollection();
        withoutProperty("connection-string").withBean("first", MongoClient.class, () -> mongoClient, definition -> {
                    definition.setPrimary(true);
                    definition.setDestroyMethodName("");
                })
                .withBean("second", MongoClient.class, () -> otherClient, definition -> definition.setDestroyMethodName(""))
                .run(context -> assertThat(context).hasSingleBean(AzureDocumentDbEmbeddingStore.class));
        clients.verifyNoInteractions();
        verify(mongoClient, never()).close();
        verifyNoInteractions(otherClient);
    }

    @Test
    void should_reject_ambiguous_mongo_clients() {
        assertFailure(withoutProperty("connection-string")
                        .withBean("first", MongoClient.class, () -> mongoClient, definition -> definition.setDestroyMethodName(""))
                        .withBean("second", MongoClient.class, () -> otherClient, definition -> definition.setDestroyMethodName("")),
                NoUniqueBeanDefinitionException.class, "MongoClient");
    }

    @Test
    void should_close_owned_client_on_context_shutdown() {
        ownedClient();
        configuredRunner.run(context -> {
            assertThat(context).hasSingleBean(AzureDocumentDbEmbeddingStore.class);
            verify(mongoClient, never()).close();
        });
        verify(mongoClient).close();
    }

    @Test
    void should_close_owned_client_only_once_even_after_manual_close() {
        ownedClient();
        configuredRunner.run(context -> {
            AzureDocumentDbEmbeddingStore store = context.getBean(AzureDocumentDbEmbeddingStore.class);
            store.close();
            store.close();
        });
        verify(mongoClient).close();
    }

    @Test
    void should_leave_spring_managed_client_destruction_to_spring() {
        existingCollection();
        withoutProperty("connection-string")
                .withBean(MongoClient.class, () -> mongoClient, definition -> definition.setDestroyMethodName("close"))
                .run(context -> {
                    context.getBean(AzureDocumentDbEmbeddingStore.class).close();
                    verify(mongoClient, never()).close();
                });
        verify(mongoClient).close();
        clients.verifyNoInteractions();
    }

    @Test
    void should_close_owned_client_on_initialization_failure() {
        clients.when(() -> MongoClients.create(any(MongoClientSettings.class))).thenReturn(mongoClient);
        when(mongoClient.getDatabase("database")).thenThrow(new IllegalStateException("initialization failed"));
        configuredRunner.run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).hasRootCauseMessage("initialization failed");
        });
        verify(mongoClient).close();
    }

    @Test
    void should_not_close_caller_client_on_initialization_failure() {
        when(mongoClient.getDatabase("database")).thenThrow(new IllegalStateException("initialization failed"));
        withoutProperty("connection-string")
                .withBean(MongoClient.class, () -> mongoClient, definition -> definition.setDestroyMethodName(""))
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasRootCauseMessage("initialization failed");
                });
        verify(mongoClient, never()).close();
        clients.verifyNoInteractions();
    }

    @Test
    void should_close_owned_client_if_another_bean_fails() {
        ownedClient();
        configuredRunner.withBean("failingBean", Object.class, () -> {
                    throw new IllegalStateException("another bean failed");
                }, definition -> definition.setDependsOn("azureDocumentDbEmbeddingStore"))
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasRootCauseMessage("another bean failed");
                });
        verify(mongoClient).close();
    }

    @Test
    void should_discover_starter_without_reusing_boots_default_mongo_client() {
        ownedClient();
        clients.when(() -> MongoClients.create(any(MongoClientSettings.class), any(MongoDriverInformation.class)))
                .thenReturn(otherClient);

        new ApplicationContextRunner().withUserConfiguration(TestApplication.class)
                .withPropertyValues(PROPERTIES)
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureDocumentDbEmbeddingStore.class);
                    assertThat(context.getBean(MongoClient.class)).isSameAs(otherClient);
                });
        verify(mongoClient).close();
        verify(otherClient).close();
        verify(otherClient, never()).getDatabase(anyString());
        assertThat(createdSettings().getApplicationName()).isEqualTo("LangChain4j");
    }

    @Test
    void should_not_use_boots_default_client_when_connection_string_is_missing() {
        new ApplicationContextRunner().withUserConfiguration(TestApplication.class)
                .withPropertyValues(PREFIX + ".database-name=database", PREFIX + ".collection-name=embeddings",
                        PREFIX + ".kind=vector-ivf")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasRootCauseInstanceOf(IllegalArgumentException.class)
                            .hasStackTraceContaining("'langchain4j.azure.documentdb.connection-string'")
                            .hasStackTraceContaining("auto-configured by Spring Boot is not used");
                });
        clients.verifyNoInteractions();
    }

    @Test
    void should_discover_starter_with_a_user_mongo_client() {
        existingCollection();
        new ApplicationContextRunner().withUserConfiguration(TestApplication.class)
                .withPropertyValues(Arrays.copyOfRange(PROPERTIES, 1, PROPERTIES.length))
                .withBean(MongoClient.class, () -> mongoClient, definition -> definition.setDestroyMethodName(""))
                .run(context -> assertThat(context).hasSingleBean(AzureDocumentDbEmbeddingStore.class));
        clients.verifyNoInteractions();
        verify(mongoClient, never()).close();
    }

    @Test
    void should_not_recreate_existing_index() {
        ownedClient();
        when(collection.listIndexes()).thenReturn(indexes);
        when(indexes.spliterator()).thenAnswer(invocation ->
                List.of(new Document("name", "defaultIndexAzureCosmos")).spliterator());
        configuredRunner.withPropertyValues(PREFIX + ".create-index=true", PREFIX + ".dimensions=3")
                .run(context -> assertThat(context).hasSingleBean(AzureDocumentDbEmbeddingStore.class));
        verify(database, never()).runCommand(any(Bson.class));
    }

    @Test
    void should_not_accept_legacy_properties_as_documentdb_configuration() {
        assertFailure(contextRunner.withPropertyValues(
                        "langchain4j.azure.cosmos-mongo-vcore.connection-string=" + CONNECTION_STRING,
                        "langchain4j.azure.cosmos-mongo-vcore.database-name=database",
                        "langchain4j.azure.cosmos-mongo-vcore.collection-name=embeddings",
                        "langchain4j.azure.cosmos-mongo-vcore.kind=vector-ivf"),
                IllegalArgumentException.class, "'langchain4j.azure.documentdb.connection-string'");
    }

    @Test
    void should_publish_metadata_with_core_defaults_and_required_kind() throws IOException {
        try (InputStream stream = getClass().getResourceAsStream("/META-INF/spring-configuration-metadata.json")) {
            assertThat(stream).isNotNull();
            Document metadata = Document.parse(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
            Map<String, Document> properties = metadata.getList("properties", Document.class).stream()
                    .collect(Collectors.toMap(property -> property.getString("name"), Function.identity()));
            assertThat(properties).hasSize(13);
            assertDefault(properties, "enabled", true);
            assertDefault(properties, "create-index", false);
            assertDefault(properties, "index-name", "defaultIndexAzureCosmos");
            assertDefault(properties, "application-name", "LangChain4j");
            assertDefault(properties, "num-lists", 1);
            assertDefault(properties, "m", 16);
            assertDefault(properties, "ef-construction", 64);
            assertDefault(properties, "ef-search", 40);
            assertThat(properties.get(PREFIX + ".dimensions")).doesNotContainKey("defaultValue");
            assertThat(properties.get(PREFIX + ".kind")).doesNotContainKey("defaultValue");
            assertThat(properties.get(PREFIX + ".connection-string")).doesNotContainKey("defaultValue");
            Document kindHint = metadata.getList("hints", Document.class).stream()
                    .filter(hint -> hint.getString("name").equals(PREFIX + ".kind"))
                    .findFirst().orElseThrow();
            assertThat(kindHint.getList("values", Document.class))
                    .extracting(value -> value.getString("value")).containsExactly("vector-ivf", "vector-hnsw");
        }
    }

    private void assertDefault(Map<String, Document> properties, String name, Object value) {
        assertThat(properties.get(PREFIX + "." + name)).containsEntry("defaultValue", value);
    }

    private ApplicationContextRunner withoutProperty(String property) {
        return contextRunner.withPropertyValues(Arrays.stream(PROPERTIES)
                .filter(value -> !value.startsWith(PREFIX + "." + property + "="))
                .toArray(String[]::new));
    }

    private void assertFailure(ApplicationContextRunner runner, Class<? extends Throwable> cause, String message) {
        runner.run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).hasRootCauseInstanceOf(cause).hasStackTraceContaining(message);
            assertThat(context.getStartupFailure()).rootCause().hasMessageNotContaining(CONNECTION_STRING);
        });
        clients.verifyNoInteractions();
    }

    private void ownedClient() {
        clients.when(() -> MongoClients.create(any(MongoClientSettings.class))).thenReturn(mongoClient);
        existingCollection();
    }

    private void existingCollection() {
        when(mongoClient.getDatabase("database")).thenReturn(database);
        when(database.listCollectionNames()).thenReturn(collectionNames);
        when(collectionNames.spliterator()).thenAnswer(invocation -> List.of("embeddings").spliterator());
        doReturn(collection).when(database).getCollection(eq("embeddings"), any());
        doReturn(collection).when(collection).withCodecRegistry(any());
    }

    private void missingIndex() {
        when(collection.listIndexes()).thenReturn(indexes);
        when(indexes.spliterator()).thenAnswer(invocation -> List.<Document>of().spliterator());
    }

    private void emptySearchResults() {
        when(collection.aggregate(anyList(), eq(BsonDocument.class))).thenReturn(results);
        when(results.spliterator()).thenAnswer(invocation -> List.<BsonDocument>of().spliterator());
    }

    private BsonDocument createdIndex() {
        ArgumentCaptor<Bson> command = ArgumentCaptor.forClass(Bson.class);
        verify(database).runCommand(command.capture());
        BsonDocument document = command.getValue().toBsonDocument(Document.class, MongoClientSettings.getDefaultCodecRegistry());
        assertThat(document.getString("createIndexes").getValue()).isEqualTo("embeddings");
        return document.getArray("indexes").get(0).asDocument();
    }

    private BsonDocument search(AzureDocumentDbEmbeddingStore store) {
        assertThat(store.search(EmbeddingSearchRequest.builder()
                .queryEmbedding(Embedding.from(new float[]{1, 2, 3}))
                .maxResults(5)
                .build()).matches()).isEmpty();
        verify(collection).aggregate(pipeline.capture(), eq(BsonDocument.class));
        return pipeline.getValue().get(0)
                .toBsonDocument(Document.class, MongoClientSettings.getDefaultCodecRegistry()).getDocument("$search");
    }

    private MongoClientSettings createdSettings() {
        ArgumentCaptor<MongoClientSettings> settings = ArgumentCaptor.forClass(MongoClientSettings.class);
        clients.verify(() -> MongoClients.create(settings.capture()));
        return settings.getValue();
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    static class TestApplication {
    }
}
