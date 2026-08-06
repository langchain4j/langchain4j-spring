# LangChain4j Easy RAG Spring Boot Starter

This Spring Boot starter provides easy integration for Retrieval-Augmented Generation (RAG) functionality using LangChain4j.

## Features

- 🚀 **Auto-configuration** - Automatic setup of RAG components
- 📄 **Flexible Document Loading** - Support for single files, directories, and glob patterns
- 🔄 **Recursive Traversal** - Load documents from nested directory structures
- 🎯 **Glob Pattern Matching** - Filter documents by file patterns (e.g., `*.txt`, `*.md`)
- 🧩 **Customizable Components** - Override default beans for custom behavior
- 📦 **Embedded Models** - Includes BGE Small quantized embedding model by default

## Quick Start

### 1. Add Dependency

```xml
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-easy-rag-spring-boot-starter</artifactId>
    <version>1.10.0-beta18-SNAPSHOT</version>
</dependency>
```

### 2. Configure Application Properties

```yaml
langchain4j:
  easy-rag:
    ingestion:
      documents:
        path: /path/to/your/documents
```

### 3. Provide an Embedding Store Bean

```java
@Configuration
public class RagConfiguration {
    
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }
}
```

That's it! Documents will be automatically loaded and ingested on application startup.

## Configuration Options

### Basic Configuration

```yaml
langchain4j:
  easy-rag:
    ingestion:
      documents:
        # Path to documents (file or directory)
        path: /path/to/documents
        
        # Enable recursive directory traversal (default: false)
        recursion: true
        
        # Glob pattern for file filtering (e.g., *.txt, *.md)
        glob: "*.txt"
```

## Usage Examples

### Example 1: Load Single File

```yaml
langchain4j:
  easy-rag:
    ingestion:
      documents:
        path: /data/document.txt
```

### Example 2: Load All Files in Directory

```yaml
langchain4j:
  easy-rag:
    ingestion:
      documents:
        path: /data/documents
```

### Example 3: Load Files Recursively

```yaml
langchain4j:
  easy-rag:
    ingestion:
      documents:
        path: /data/documents
        recursion: true
```

### Example 4: Load Only Text Files

```yaml
langchain4j:
  easy-rag:
    ingestion:
      documents:
        path: /data/documents
        glob: "*.txt"
```

### Example 5: Load Markdown Files Recursively

```yaml
langchain4j:
  easy-rag:
    ingestion:
      documents:
        path: /data/docs
        glob: "*.md"
        recursion: true
```

## Customization

### Custom Embedding Model

Override the default embedding model by providing your own bean:

```java
@Configuration
public class CustomRagConfiguration {
    
    @Bean
    public EmbeddingModel easyRagEmbeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName("text-embedding-ada-002")
                .build();
    }
}
```

### Custom Document Splitter

Configure custom splitting behavior:

```java
@Configuration
public class CustomRagConfiguration {
    
    @Bean
    public DocumentSplitter easyRagDocumentSplitter(TokenCountEstimator estimator) {
        return DocumentSplitters.recursive(
                500,  // Max chunk size
                50,   // Overlap
                estimator
        );
    }
}
```

### Custom Embedding Store

Use a persistent embedding store:

```java
@Configuration
public class CustomRagConfiguration {
    
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return ElasticsearchEmbeddingStore.builder()
                .serverUrl("http://localhost:9200")
                .indexName("documents")
                .build();
    }
}
```

## Default Components

The starter provides the following default beans (can be overridden):

| Bean Name | Type | Description |
|-----------|------|-------------|
| `easyRagEmbeddingModel` | `EmbeddingModel` | BGE Small quantized model |
| `easyRagTokenCountEstimator` | `TokenCountEstimator` | HuggingFace token estimator |
| `easyRagDocumentSplitter` | `DocumentSplitter` | Recursive splitter (300 tokens, 30 overlap) |

## Supported File Types

The document loader supports various file formats through LangChain4j's `FileSystemDocumentLoader`:

- Plain text (`.txt`)
- Markdown (`.md`)
- PDF (`.pdf`)
- Microsoft Word (`.doc`, `.docx`)
- And more...

## How It Works

1. **Startup**: On application startup, the `easyRagDocumentIngestor` runner is executed
2. **Loading**: Documents are loaded based on the configured path, glob pattern, and recursion settings
3. **Splitting**: Documents are split into chunks using the configured `DocumentSplitter`
4. **Embedding**: Text chunks are converted to embeddings using the configured `EmbeddingModel`
5. **Storage**: Embeddings are stored in the configured `EmbeddingStore`

## Requirements

- Java 17 or higher
- Spring Boot 3.3.8 or higher

## Related Documentation

- [LangChain4j Documentation](https://docs.langchain4j.dev/)
- [Spring Boot Integration Guide](https://docs.langchain4j.dev/tutorials/spring-boot-integration)

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](../../LICENSE) file for details.

