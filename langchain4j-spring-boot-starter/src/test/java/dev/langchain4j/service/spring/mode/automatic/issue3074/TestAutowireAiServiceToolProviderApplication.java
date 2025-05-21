package dev.langchain4j.service.spring.mode.automatic.issue3074;

import dev.langchain4j.service.tool.ToolProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class TestAutowireAiServiceToolProviderApplication {
    @Bean
    public ToolProvider toolProvider() {
        return new TestMcpToolProvider();
    }

    public static void main(String[] args) {
        SpringApplication.run(TestAutowireAiServiceToolProviderApplication.class, args);
    }
}
