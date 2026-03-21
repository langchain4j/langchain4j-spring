package dev.langchain4j.voyageai.spring;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.scoring.ScoringModel;
import dev.langchain4j.model.voyageai.VoyageAiEmbeddingModel;
import dev.langchain4j.model.voyageai.VoyageAiEmbeddingModelName;
import dev.langchain4j.model.voyageai.VoyageAiScoringModel;
import dev.langchain4j.model.voyageai.VoyageAiScoringModelName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "VOYAGE_API_KEY", matches = ".+")
class AutoConfigIT {

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AutoConfig.class));

    @Test
    void should_provide_embedding_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.voyage-ai.embedding-model.api-key=" + System.getenv("VOYAGE_API_KEY"),
                        "langchain4j.voyage-ai.embedding-model.model-name=" + VoyageAiEmbeddingModelName.VOYAGE_3_LITE,
                        "langchain4j.voyage-ai.embedding-model.log-requests=true",
                        "langchain4j.voyage-ai.embedding-model.log-responses=true"
                )
                .run(context -> {

                    EmbeddingModel embeddingModel = context.getBean(EmbeddingModel.class);
                    assertThat(embeddingModel).isInstanceOf(VoyageAiEmbeddingModel.class);
                    assertThat(embeddingModel.embed("hi").content().dimension()).isEqualTo(VoyageAiEmbeddingModelName.VOYAGE_3_LITE.dimension());

                    assertThat(context.getBean(VoyageAiEmbeddingModel.class)).isSameAs(embeddingModel);
                });
    }

    @Test
    void should_provide_scoring_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.voyage-ai.scoring-model.api-key=" + System.getenv("VOYAGE_API_KEY"),
                        "langchain4j.voyage-ai.scoring-model.model-name=" + VoyageAiScoringModelName.RERANK_LITE_1,
                        "langchain4j.voyage-ai.scoring-model.log-requests=true",
                        "langchain4j.voyage-ai.scoring-model.log-responses=true"
                )
                .run(context -> {

                    ScoringModel scoringModel = context.getBean(ScoringModel.class);
                    assertThat(scoringModel).isInstanceOf(VoyageAiScoringModel.class);


                    TextSegment catSegment = TextSegment.from("The Maine Coon is a large domesticated cat breed.");
                    TextSegment dogSegment = TextSegment.from("The sweet-faced, lovable Labrador Retriever is one of America's most popular dog breeds, year after year.");
                    List<TextSegment> segments = asList(catSegment, dogSegment);

                    String query = "tell me about dogs";
                    Response<List<Double>> response = scoringModel.scoreAll(segments, query);
                    List<Double> scores = response.content();
                    assertThat(scores).hasSize(2);
                    assertThat(scores.get(0)).isLessThan(scores.get(1));

                    assertThat(context.getBean(VoyageAiScoringModel.class)).isSameAs(scoringModel);
                });
    }
}
