package dev.langchain4j.service.spring.mode.automatic.missingAnnotation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
class AiServiceWithMissingAnnotationApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiServiceWithMissingAnnotationApplication.class, args);
    }
}
