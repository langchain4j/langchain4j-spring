package dev.langchain4j.service.spring.mode.automatic.withProfiles;

import dev.langchain4j.service.spring.AiServicesAutoConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiServiceWithProfilesIT {

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfig.class));

    @Test
    void should_not_create_ai_service() {
        contextRunner
                .withPropertyValues(
                        "spring.profiles.active=test"
                )
                .withUserConfiguration(AiServiceWithProfilesApplication.class)
                .run(context -> assertThatThrownBy(() -> context.getBean(AiServiceWithProfiles.class))
                        .isInstanceOf(BeansException.class));
    }

    @Test
    void should_create_ai_service() {
        contextRunner
                .withPropertyValues(
                        "spring.profiles.active=dev"
                )
                .withUserConfiguration(AiServiceWithProfilesApplication.class)
                .run(context -> assertThat(context.getBean(AiServiceWithProfiles.class)).isNotNull());
    }
}
