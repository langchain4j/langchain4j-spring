package dev.langchain4j.rag.spring;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import dev.langchain4j.spring.LangChain4jAutoConfig;
import java.net.URISyntaxException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ConfigurationMetadataTest {

    @Test
    void shouldGenerateSpringConfigurationMetadata() throws Exception {
        assertThat(classesDirectory().resolve("META-INF/spring-configuration-metadata.json"))
                .isRegularFile()
                .content(UTF_8)
                .contains("\"" + RagProperties.PREFIX + ".retrieval.max-results\"");
    }

    @Test
    void shouldGenerateSpringAutoConfigureMetadata() throws Exception {
        assertThat(classesDirectory().resolve("META-INF/spring-autoconfigure-metadata.properties"))
                .isRegularFile()
                .content(UTF_8)
                .contains(LangChain4jAutoConfig.class.getName());
    }

    private static Path classesDirectory() throws URISyntaxException {
        return Path.of(RagProperties.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI());
    }
}
