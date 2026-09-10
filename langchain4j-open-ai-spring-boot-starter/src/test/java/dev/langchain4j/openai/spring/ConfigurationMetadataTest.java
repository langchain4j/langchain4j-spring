package dev.langchain4j.openai.spring;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URISyntaxException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ConfigurationMetadataTest {

    @Test
    void shouldGenerateSpringConfigurationMetadata() throws Exception {
        assertThat(classesDirectory().resolve("META-INF/spring-configuration-metadata.json"))
                .isRegularFile()
                .content(UTF_8)
                .contains("\"" + Properties.PREFIX + ".chat-model.api-key\"");
    }

    @Test
    void shouldGenerateSpringAutoConfigureMetadata() throws Exception {
        assertThat(classesDirectory().resolve("META-INF/spring-autoconfigure-metadata.properties"))
                .isRegularFile()
                .content(UTF_8)
                .contains(AutoConfig.class.getName());
    }

    private static Path classesDirectory() throws URISyntaxException {
        return Path.of(Properties.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI());
    }
}
