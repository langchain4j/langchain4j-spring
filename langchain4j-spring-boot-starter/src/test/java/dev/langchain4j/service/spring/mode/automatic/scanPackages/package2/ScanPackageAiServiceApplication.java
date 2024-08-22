package dev.langchain4j.service.spring.mode.automatic.scanPackages.package2;

import dev.langchain4j.service.spring.mode.automatic.scanPackages.aiService.ScanPackageAiService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackageClasses = ScanPackageAiService.class)
public class ScanPackageAiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScanPackageAiServiceApplication.class, args);
    }
}