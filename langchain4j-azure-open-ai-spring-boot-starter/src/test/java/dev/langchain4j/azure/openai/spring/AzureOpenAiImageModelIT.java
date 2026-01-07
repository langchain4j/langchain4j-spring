package dev.langchain4j.azure.openai.spring;

import dev.langchain4j.data.image.Image;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        properties = {
                "langchain4j.azure-open-ai.image-model.api-key=${AZURE_OPENAI_KEY}",
                "langchain4j.azure-open-ai.image-model.endpoint=${AZURE_OPENAI_ENDPOINT}",
                "langchain4j.azure-open-ai.image-model.deployment-name=dall-e-3-30"
        },
        classes = AzureOpenAiImageModelIT.TestApplication.class
)
@EnabledIfEnvironmentVariable(named = "AZURE_OPENAI_KEY", matches = ".+")
class AzureOpenAiImageModelIT {

    @Autowired
    ImageModel imageModel;

    @Test
    void should_generate_image() {
        Response<Image> coffee = imageModel.generate("coffee");

        assertThat(coffee.content().url()).isNotNull();
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApplication {
    }
}
