package dev.langchain4j.service.spring.mode.automatic.scanPackages.package1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"dev.langchain4j.service.spring.mode.automatic.scanPackages.aiService"})
public class ScanPackageAiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScanPackageAiServiceApplication.class, args);
    }
}