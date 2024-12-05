package dev.langchain4j.service.spring.mode.explicit.tools;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
class AiServiceWithExplicitToolsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiServiceWithExplicitToolsApplication.class, args);
    }
}
