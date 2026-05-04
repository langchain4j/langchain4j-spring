package dev.langchain4j.service.spring.mode.automatic.withModerationModel;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.moderation.Moderation;
import dev.langchain4j.model.moderation.ModerationModel;
import dev.langchain4j.model.output.Response;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
class AiServiceWithModerationModelApplication {

    @Bean
    ModerationModel moderationModel() {
        return new ModerationModel() {
            @Override
            public Response<Moderation> moderate(String s) {
                return Response.from(Moderation.flagged("Flagged"));
            }

            @Override
            public Response<Moderation> moderate(List<ChatMessage> list) {
                return Response.from(Moderation.flagged("Flagged"));
            }
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(AiServiceWithModerationModelApplication.class, args);
    }
}
