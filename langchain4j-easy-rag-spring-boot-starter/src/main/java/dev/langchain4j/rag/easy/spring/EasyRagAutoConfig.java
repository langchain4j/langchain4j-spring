package dev.langchain4j.rag.easy.spring;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.TokenCountEstimator;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.HuggingFaceTokenCountEstimator;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static dev.langchain4j.internal.Utils.isNotNullOrBlank;
import static dev.langchain4j.rag.easy.spring.EasyRagProperties.PREFIX;
import static java.util.Collections.singletonList;

@AutoConfiguration
@EnableConfigurationProperties(EasyRagProperties.class)
public class EasyRagAutoConfig {

    private static final Logger log = LoggerFactory.getLogger(EasyRagAutoConfig.class);

    /**
     * Provides a default token count estimator for document splitting.
     * Can be overridden by providing a custom TokenCountEstimator bean.
     */
    @Bean("easyRagTokenCountEstimator")
    @ConditionalOnMissingBean
    @ConditionalOnClass(HuggingFaceTokenCountEstimator.class)
    TokenCountEstimator easyRagTokenCountEstimator() {
        return new HuggingFaceTokenCountEstimator();
    }

    /**
     * Provides a default document splitter with recursive splitting strategy.
     * Default configuration: 300 tokens per chunk, 30 tokens overlap.
     * Can be overridden by providing a custom DocumentSplitter bean.
     */
    @Bean("easyRagDocumentSplitter")
    @ConditionalOnMissingBean
    @ConditionalOnClass(DocumentSplitters.class)
    DocumentSplitter easyRagDocumentSplitter(TokenCountEstimator tokenCountEstimator) {
        return DocumentSplitters.recursive(300, 30, tokenCountEstimator);
    }

    /**
     * Provides a default embedding model using BGE Small quantized model.
     * Can be overridden by providing a custom EmbeddingModel bean.
     */
    @Bean("easyRagEmbeddingModel")
    @ConditionalOnMissingBean
    @ConditionalOnClass(BgeSmallEnV15QuantizedEmbeddingModel.class)
    EmbeddingModel easyRagEmbeddingModel() {
        return new BgeSmallEnV15QuantizedEmbeddingModel();
    }

    /**
     * Application runner that ingests documents into the embedding store on startup.
     * Activated when langchain4j.easy-rag.ingestion.documents.path is configured.
     * Supports:
     * - Single file or directory ingestion
     * - Recursive directory traversal
     * - Glob pattern matching for file filtering
     */
    @Bean
    @ConditionalOnProperty(PREFIX + ".ingestion.documents.path")
    ApplicationRunner easyRagDocumentIngestor(DocumentSplitter documentSplitter,
                                              EmbeddingModel embeddingModel,
                                              EmbeddingStore<TextSegment> embeddingStore,
                                              EasyRagProperties easyRagProperties) {
        return args -> {
            List<Document> documents = loadDocuments(easyRagProperties.ingestion.documents);

            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .documentSplitter(documentSplitter)
                    .embeddingStore(embeddingStore)
                    .embeddingModel(embeddingModel)
                    .build();

            ingestor.ingest(documents);
        };
    }

    private static List<Document> loadDocuments(DocumentsProperties documentsProperties) {
        Path path = documentsProperties.path;
        String glob = documentsProperties.glob;
        boolean recursive = documentsProperties.recursion != null && documentsProperties.recursion;

        try {
            // Single file
            if (Files.isRegularFile(path)) {
                Document document = FileSystemDocumentLoader.loadDocument(path);
                return singletonList(document);
            }

            // Directory with glob pattern
            if (isNotNullOrBlank(glob)) {
                return loadDocumentsWithGlob(path, glob, recursive);
            }

            // Directory without glob (all files)
            if (recursive) {
                return loadDocumentsRecursively(path);
            } else {
                return FileSystemDocumentLoader.loadDocuments(path);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load documents from path: " + path, e);
        }
    }

    private static List<Document> loadDocumentsWithGlob(Path basePath, String globPattern, boolean recursive) throws IOException {
        List<Document> documents = new ArrayList<>();
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + globPattern);

        Files.walkFileTree(basePath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (matcher.matches(file.getFileName())) {
                    try {
                        Document document = FileSystemDocumentLoader.loadDocument(file);
                        documents.add(document);
                    } catch (Exception e) {
                        // Log and continue with other files
                        log.warn("Failed to load document: {}, error: {}", file, e.getMessage());
                    }
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                // Skip subdirectories if not recursive
                if (!recursive && !dir.equals(basePath)) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }
        });

        return documents;
    }

    private static List<Document> loadDocumentsRecursively(Path basePath) throws IOException {
        List<Document> documents = new ArrayList<>();

        Files.walkFileTree(basePath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                try {
                    Document document = FileSystemDocumentLoader.loadDocument(file);
                    documents.add(document);
                } catch (Exception e) {
                    // Log and continue with other files
                    log.warn("Failed to load document: {}, error: {}", file, e.getMessage());
                }
                return FileVisitResult.CONTINUE;
            }
        });

        return documents;
    }
}
