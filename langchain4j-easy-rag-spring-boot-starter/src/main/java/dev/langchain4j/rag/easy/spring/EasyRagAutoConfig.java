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
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

import static dev.langchain4j.internal.Utils.isNotNullOrBlank;
import static dev.langchain4j.rag.easy.spring.EasyRagProperties.PREFIX;
import static java.util.Collections.singletonList;

@AutoConfiguration
@EnableConfigurationProperties(EasyRagProperties.class)
public class EasyRagAutoConfig {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(HuggingFaceTokenCountEstimator.class)
    TokenCountEstimator tokenCountEstimator() { // TODO bean name, type
        return new HuggingFaceTokenCountEstimator();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(DocumentSplitters.class)
    DocumentSplitter documentSplitter(TokenCountEstimator tokenCountEstimator) { // TODO bean name, type
        return DocumentSplitters.recursive(300, 30, tokenCountEstimator);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(BgeSmallEnV15QuantizedEmbeddingModel.class)
    EmbeddingModel embeddingModel() { // TODO bean name, type
        return new BgeSmallEnV15QuantizedEmbeddingModel();
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".ingestion.documents.path")
    ApplicationRunner easyRagDocumentIngestor(DocumentSplitter documentSplitter, // TODO should splitter be optional?
                                              EmbeddingModel embeddingModel,
                                              EmbeddingStore<TextSegment> embeddingStore,
                                              EasyRagProperties easyRagProperties) { // TODO bean name, type
        return args -> {

            List<Document> documents = loadDocuments(easyRagProperties.ingestion().documents());

            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    // TODO documentTransformer
                    .documentSplitter(documentSplitter)
                    // TODO textSegmentTransformer
                    .embeddingStore(embeddingStore)
                    .embeddingModel(embeddingModel)
                    .build();

            ingestor.ingest(documents);
        };
    }

    private static List<Document> loadDocuments(DocumentsProperties documentsProperties) {
        if (documentsProperties.recursion() != null && documentsProperties.recursion()) {
            throw new NotImplementedException(); // TODO
        } else {
            if (isNotNullOrBlank(documentsProperties.glob())) {
                throw new NotImplementedException();  // TODO
            } else {
                if (documentsProperties.path().toFile().isDirectory()) {
                    return FileSystemDocumentLoader.loadDocuments(documentsProperties.path());
                } else {
                    Document document = FileSystemDocumentLoader.loadDocument(documentsProperties.path());
                    return singletonList(document);
                }
            }
        }
    }

    // TODO ITs
}
