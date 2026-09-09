package dev.langchain4j.rag.spring;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ConfigurationMetadataTest {

    @Test
    void shouldGenerateSpringConfigurationMetadata() throws Exception {
        Path classesDirectory =
                Path.of(RagProperties.class.getProtectionDomain().getCodeSource().getLocation().toURI());

        assertThat(classesDirectory.resolve("META-INF/spring-configuration-metadata.json")).isRegularFile();
    }
}
