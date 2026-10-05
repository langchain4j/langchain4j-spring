# langchain4j-spring
This repository contains Spring Boot starters for popular integrations.
Starters for other integrations will be added with time.

If you have any issues or feature requests, please submit them [here](https://github.com/langchain4j/langchain4j/issues/new/choose).

### [Documentation](https://docs.langchain4j.dev/tutorials/spring-boot-integration)

## Azure DocumentDB

Choose exactly one starter matching your application's Spring Boot major version:

| Spring Boot | Artifact (`dev.langchain4j`) |
| --- | --- |
| 3 | `langchain4j-azure-documentdb-spring-boot-starter` |
| 4 | `langchain4j-azure-documentdb-spring-boot4-starter` |

For example, for Spring Boot 3:

```xml
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-azure-documentdb-spring-boot-starter</artifactId>
    <version>${langchain4j.version}</version>
</dependency>
```

For Spring Boot 4, replace the artifact ID with `langchain4j-azure-documentdb-spring-boot4-starter`.
Use a compatible LangChain4j Spring version for the `langchain4j.version` Maven property,
or omit the dependency version when importing the matching `langchain4j-spring-bom`.
These new starters and their `langchain4j-azure-documentdb` dependency are not yet released.
This development branch uses `1.22.0-beta32-SNAPSHOT` with core `1.22.0-SNAPSHOT`;
build and install the matching core and Spring reactor snapshots locally until they are published.
The dependency example does not imply that these artifacts are already available as a release.

### Configuration

Configure the store in `application.properties`, using environment variables for connection details:

```properties
langchain4j.azure.documentdb.connection-string=${AZURE_DOCUMENTDB_CONNECTION_STRING}
langchain4j.azure.documentdb.database-name=${AZURE_DOCUMENTDB_DATABASE}
langchain4j.azure.documentdb.collection-name=embeddings
langchain4j.azure.documentdb.kind=vector-ivf
langchain4j.azure.documentdb.create-index=true
langchain4j.azure.documentdb.dimensions=${EMBEDDING_DIMENSIONS}
```

The starter provides an injectable `AzureDocumentDbEmbeddingStore`
(`dev.langchain4j.store.embedding.azure.documentdb.AzureDocumentDbEmbeddingStore`).
It backs off when you declare your own bean of that type. Set
`langchain4j.azure.documentdb.enabled=false` to disable this auto-configuration.
When enabled, missing required configuration fails application startup rather than silently disabling the store.

All the following properties use the prefix `langchain4j.azure.documentdb`:

| Property | Default / requirement |
| --- | --- |
| `enabled` | `true` |
| `connection-string` | Required unless a user-provided `MongoClient` bean is available |
| `database-name` | Required |
| `collection-name` | Required |
| `kind` | Required: `vector-ivf` or `vector-hnsw`, even when `create-index=false` |
| `create-index` | `false`; set to `true` to create a missing vector index |
| `dimensions` | No default; must be positive if supplied |
| `index-name` | `defaultIndexAzureCosmos`, preserved for migration compatibility |
| `application-name` | `LangChain4j`; used only for an internally created client, overriding the URI's application name |
| `num-lists` | `1`; IVF index clusters |
| `m` | `16`; HNSW connections per layer |
| `ef-construction` | `64`; HNSW index construction candidate list size |
| `ef-search` | `40`; HNSW search candidate list size |

When `create-index=true`, supply dimensions matching your embedding model, or omit `dimensions`
and provide an `EmbeddingModel` bean from which the starter can obtain `dimension()`.
An explicit dimension value takes precedence. With multiple model beans, designate one `@Primary`
or set dimensions explicitly; ambiguous models fail startup rather than being chosen arbitrarily.
The model is not consulted when dimensions are explicit or index creation is disabled.
There is no implicit 1536-dimensional fallback. If `create-index=false`, dimensions can be omitted,
but `kind` must still match the existing index because it controls the search query.
The store can create a missing collection regardless of `create-index`.

For HNSW, set `kind=vector-hnsw` and, if needed, configure `m`, `ef-construction`, and `ef-search`.
Choose settings supported by your existing Azure DocumentDB cluster; see the
[Azure DocumentDB vector search documentation](https://learn.microsoft.com/azure/documentdb/vector-search).

### Client ownership

A user-defined `com.mongodb.client.MongoClient` bean takes precedence over `connection-string`.
With multiple clients, designate one `@Primary` or configure your own `AzureDocumentDbEmbeddingStore` bean.
An ambiguous set of clients fails startup, even if a connection string is present.
The starter runs before Spring Boot's MongoDB auto-configuration, so Boot's automatically created
default client does not override the DocumentDB connection string or substitute for missing connection details.
Other Spring Boot MongoDB auto-configuration is otherwise unchanged.

Spring closes the embedding store on application shutdown. A client created by the store from a connection
string is closed exactly once, including after an initialization failure. A supplied client remains
caller-owned: closing the store never closes it. Spring can still close a supplied `MongoClient` bean
according to that bean's own destruction policy.

### Migrating from the Cosmos DB MongoDB vCore starter

Replace `langchain4j-azure-cosmos-mongo-vcore-spring-boot-starter` with the Boot 3 DocumentDB starter,
or replace `langchain4j-azure-cosmos-mongo-vcore-spring-boot4-starter` with the Boot 4 variant.
Rename the property prefix from `langchain4j.azure.cosmos-mongo-vcore` to `langchain4j.azure.documentdb`
and update Java imports to `dev.langchain4j.store.embedding.azure.documentdb.AzureDocumentDbEmbeddingStore`.
Do not include both the legacy and replacement starters for the same store.

Set `kind` explicitly to match your existing IVF or HNSW index; the new starter does not inherit
the legacy starter's implicit IVF choice. When creating an index, configure dimensions explicitly
or provide an unambiguous embedding model. Keep the same database, collection, and index name
when reusing existing data. The default index name remains `defaultIndexAzureCosmos`, so migration
does not create a differently named index by default. Legacy starter artifacts, properties, and behavior
remain unchanged; their property names are not aliases for the new prefix.
