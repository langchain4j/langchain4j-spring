package dev.langchain4j.spring.it;

import org.springframework.boot.webclient.WebClientCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
class TestApplication {

    static final String CUSTOMIZER_HEADER = "X-Application";

    @Bean
    WebClientCustomizer applicationHeaderCustomizer() {
        return webClientBuilder -> webClientBuilder.defaultHeader(CUSTOMIZER_HEADER, "test-application");
    }
}
