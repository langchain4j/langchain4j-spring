package dev.langchain4j.service.spring.mode.automatic.Issue2133;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author: qing
 * @Date: 2024/11/20
 */
@SpringBootApplication
public class TestAutowireAiServiceApplication {

    @Autowired
    TestAutowireConfiguration testAutowireConfiguration;

    public static void main(String[] args) {
        SpringApplication.run(TestAutowireAiServiceApplication.class, args);
    }

    TestAutowireConfiguration getConfiguration() {
        return testAutowireConfiguration;
    }
}
