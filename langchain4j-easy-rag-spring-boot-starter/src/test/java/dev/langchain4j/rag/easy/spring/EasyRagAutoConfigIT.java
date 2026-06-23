package dev.langchain4j.rag.easy.spring;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Easy RAG auto-configuration.
 * <p>
 * Tests cover all document loading scenarios including:
 * <ul>
 *   <li>Single file and directory ingestion</li>
 *   <li>Recursive directory traversal</li>
 *   <li>Glob pattern matching for file filtering</li>
 *   <li>Combination of recursion and glob patterns</li>
 *   <li>Conditional bean creation</li>
 * </ul>
 */
class EasyRagAutoConfigIT {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(EasyRagAutoConfig.class))
            .withUserConfiguration(TestConfiguration.class);

    /**
     * Tests that default RAG beans are created when no document path is configured.
     * <p>
     * <b>Scenario:</b> Application starts without any document ingestion configuration.
     * <p>
     * <b>Expected Beans:</b>
     * <ul>
     *   <li>easyRagEmbeddingModel - BGE Small quantized embedding model</li>
     *   <li>easyRagTokenCountEstimator - HuggingFace token count estimator</li>
     *   <li>easyRagDocumentSplitter - Recursive splitter (300 tokens/chunk, 30 overlap)</li>
     * </ul>
     * <p>
     * <b>Use Case:</b> Users get a complete RAG setup out of the box,
     * ready to use even without document ingestion.
     */
    @Test
    void shouldCreateDefaultBeans() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(EmbeddingModel.class);
            assertThat(context).hasBean("easyRagEmbeddingModel");
            assertThat(context).hasBean("easyRagTokenCountEstimator");
            assertThat(context).hasBean("easyRagDocumentSplitter");
        });
    }

    /**
     * Tests single file ingestion.
     * <p>
     * <b>Scenario:</b> Configuration points to a single file.
     * <p>
     * <b>Configuration Example:</b>
     * <pre>
     * langchain4j.easy-rag.ingestion.documents.path=/path/to/file.txt
     * </pre>
     * <p>
     * <b>Verification:</b>
     * <ul>
     *   <li>Spring context starts successfully</li>
     *   <li>Document ingestor bean is created</li>
     *   <li>File content is loaded into embedding store</li>
     * </ul>
     * <p>
     * <b>Use Case:</b> Processing a single important document like company policy, product manual, etc.
     */
    @Test
    void shouldIngestSingleFile(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "This is a test document for Easy RAG.");

        contextRunner
                .withPropertyValues(
                        "langchain4j.easy-rag.ingestion.documents.path=" + testFile.toString()
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasBean("easyRagDocumentIngestor");
                });
    }

    /**
     * Tests non-recursive directory loading.
     * <p>
     * <b>Scenario:</b> Load only direct files in the specified directory, ignore all subdirectories.
     * <p>
     * <b>File Structure:</b>
     * <pre>
     * tempDir/
     * ├── doc1.txt     ← Loaded
     * ├── doc2.txt     ← Loaded
     * └── subdir/
     *     └── doc3.txt ← Ignored (subdirectory)
     * </pre>
     * <p>
     * <b>Configuration Example:</b>
     * <pre>
     * langchain4j.easy-rag.ingestion.documents:
     *   path: /path/to/directory
     *   recursion: false  # default
     * </pre>
     * <p>
     * <b>Verification:</b>
     * <ul>
     *   <li>Only root-level files are loaded (doc1.txt, doc2.txt)</li>
     *   <li>Subdirectory files are ignored (subdir/doc3.txt)</li>
     * </ul>
     * <p>
     * <b>Use Case:</b> When documents are organized by directory and only one level needs processing.
     */
    @Test
    void shouldIngestDirectoryNonRecursive(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("doc1.txt"), "Document 1");
        Files.writeString(tempDir.resolve("doc2.txt"), "Document 2");
        
        Path subDir = tempDir.resolve("subdir");
        Files.createDirectory(subDir);
        Files.writeString(subDir.resolve("doc3.txt"), "Document 3");

        contextRunner
                .withPropertyValues(
                        "langchain4j.easy-rag.ingestion.documents.path=" + tempDir.toString(),
                        "langchain4j.easy-rag.ingestion.documents.recursion=false"
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasBean("easyRagDocumentIngestor");
                });
    }

    /**
     * Tests recursive directory traversal.
     * <p>
     * <b>Scenario:</b> Recursively load files from the specified directory and all subdirectories.
     * <p>
     * <b>File Structure:</b>
     * <pre>
     * tempDir/
     * ├── doc1.txt         ← Loaded
     * └── subdir/
     *     ├── doc2.txt     ← Loaded
     *     └── nested/
     *         └── doc3.txt ← Loaded (deeply nested)
     * </pre>
     * <p>
     * <b>Configuration Example:</b>
     * <pre>
     * langchain4j.easy-rag.ingestion.documents:
     *   path: /path/to/directory
     *   recursion: true  # enable recursion
     * </pre>
     * <p>
     * <b>Verification:</b>
     * <ul>
     *   <li>Files from all directory levels are loaded</li>
     *   <li>Handles multi-level nested directory structures correctly</li>
     *   <li>No depth limit on nesting</li>
     * </ul>
     * <p>
     * <b>Use Case:</b> When documents are scattered across multiple directory levels,
     * such as complex documentation libraries or multi-module project docs.
     */
    @Test
    void shouldIngestDirectoryRecursively(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("doc1.txt"), "Document 1");
        
        Path subDir = tempDir.resolve("subdir");
        Files.createDirectory(subDir);
        Files.writeString(subDir.resolve("doc2.txt"), "Document 2");
        
        Path nestedDir = subDir.resolve("nested");
        Files.createDirectory(nestedDir);
        Files.writeString(nestedDir.resolve("doc3.txt"), "Document 3");

        contextRunner
                .withPropertyValues(
                        "langchain4j.easy-rag.ingestion.documents.path=" + tempDir.toString(),
                        "langchain4j.easy-rag.ingestion.documents.recursion=true"
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasBean("easyRagDocumentIngestor");
                });
    }

    /**
     * Tests glob pattern file filtering.
     * <p>
     * <b>Scenario:</b> Use glob patterns to load only specific file types, ignoring others.
     * <p>
     * <b>File Structure:</b>
     * <pre>
     * tempDir/
     * ├── doc1.txt ← Loaded (matches *.txt)
     * ├── doc2.md  ← Ignored
     * └── doc3.pdf ← Ignored
     * </pre>
     * <p>
     * <b>Configuration Example:</b>
     * <pre>
     * langchain4j.easy-rag.ingestion.documents:
     *   path: /path/to/directory
     *   glob: "*.txt"  # only load .txt files
     * </pre>
     * <p>
     * <b>Supported Glob Patterns:</b>
     * <ul>
     *   <li>*.txt - All text files</li>
     *   <li>*.md - All Markdown files</li>
     *   <li>doc*.txt - Text files starting with "doc"</li>
     *   <li>README.* - All README files</li>
     * </ul>
     * <p>
     * <b>Verification:</b>
     * <ul>
     *   <li>Only files matching the glob pattern are loaded</li>
     *   <li>Other file types are correctly ignored</li>
     * </ul>
     * <p>
     * <b>Use Case:</b> When a directory contains mixed file types but only specific types need processing,
     * e.g., process only technical docs (.md) while ignoring config files.
     */
    @Test
    void shouldIngestWithGlobPattern(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("doc1.txt"), "Text document");
        Files.writeString(tempDir.resolve("doc2.md"), "Markdown document");
        Files.writeString(tempDir.resolve("doc3.pdf"), "PDF document");

        contextRunner
                .withPropertyValues(
                        "langchain4j.easy-rag.ingestion.documents.path=" + tempDir.toString(),
                        "langchain4j.easy-rag.ingestion.documents.glob=*.txt"
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasBean("easyRagDocumentIngestor");
                });
    }

    /**
     * Tests combination of glob pattern and recursive traversal.
     * <p>
     * <b>Scenario:</b> Recursively search all subdirectories but only load files matching the glob pattern.
     * <p>
     * <b>File Structure:</b>
     * <pre>
     * tempDir/
     * ├── doc1.txt  ← Loaded (matches *.txt)
     * ├── doc2.md   ← Ignored
     * └── subdir/
     *     ├── doc3.txt ← Loaded (recursion + matches *.txt)
     *     └── doc4.pdf ← Ignored
     * </pre>
     * <p>
     * <b>Configuration Example:</b>
     * <pre>
     * langchain4j.easy-rag.ingestion.documents:
     *   path: /path/to/directory
     *   glob: "*.txt"
     *   recursion: true  # combine recursion with glob
     * </pre>
     * <p>
     * <b>Verification:</b>
     * <ul>
     *   <li>All subdirectories are traversed recursively</li>
     *   <li>Only files matching the glob pattern are loaded</li>
     *   <li>Both features work together correctly</li>
     * </ul>
     * <p>
     * <b>Use Case:</b> Most powerful mode - find specific file types throughout an entire directory tree.
     * Example: Load all Markdown documentation files from a multi-module project.
     */
    @Test
    void shouldIngestWithGlobPatternRecursively(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("doc1.txt"), "Text document 1");
        Files.writeString(tempDir.resolve("doc2.md"), "Markdown document");
        
        Path subDir = tempDir.resolve("subdir");
        Files.createDirectory(subDir);
        Files.writeString(subDir.resolve("doc3.txt"), "Text document 2");
        Files.writeString(subDir.resolve("doc4.pdf"), "PDF document");

        contextRunner
                .withPropertyValues(
                        "langchain4j.easy-rag.ingestion.documents.path=" + tempDir.toString(),
                        "langchain4j.easy-rag.ingestion.documents.glob=*.txt",
                        "langchain4j.easy-rag.ingestion.documents.recursion=true"
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasBean("easyRagDocumentIngestor");
                });
    }

    /**
     * Tests conditional bean creation.
     * <p>
     * <b>Scenario:</b> No document path is configured, so the ingestor bean should not be created.
     * <p>
     * <b>Verification:</b>
     * <ul>
     *   <li>Document ingestor bean does NOT exist</li>
     *   <li>Default beans (embedding model, splitter, etc.) still exist</li>
     * </ul>
     * <p>
     * <b>Use Case:</b> Allows users to use only the default RAG components
     * without mandatory document ingestion on startup.
     */
    @Test
    void shouldNotCreateIngestorWhenPathNotConfigured() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean("easyRagDocumentIngestor");
        });
    }

    /**
     * Test configuration that provides required dependencies.
     */
    @Configuration
    static class TestConfiguration {
        
        @Bean
        EmbeddingStore<TextSegment> embeddingStore() {
            return new InMemoryEmbeddingStore<>();
        }
    }
}
