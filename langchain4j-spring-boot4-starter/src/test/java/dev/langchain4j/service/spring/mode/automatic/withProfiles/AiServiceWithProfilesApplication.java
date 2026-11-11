package dev.langchain4j.service.spring.mode.automatic.withProfiles;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.test.mock.MockChatModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AiServiceWithProfilesApplication {

    @Bean
    ChatModel chatModel() {
        return MockChatModel.thatResponds("Berlin");
    }

    public static void main(String[] args) {
        SpringApplication.run(AiServiceWithProfilesApplication.class, args);
    }
}
