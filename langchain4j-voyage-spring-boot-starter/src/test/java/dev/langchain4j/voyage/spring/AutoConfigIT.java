package dev.langchain4j.voyage.spring;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.scoring.ScoringModel;
import dev.langchain4j.model.voyage.VoyageEmbeddingModel;
import dev.langchain4j.model.voyage.VoyageEmbeddingModelName;
import dev.langchain4j.model.voyage.VoyageScoringModel;
import dev.langchain4j.model.voyage.VoyageScoringModelName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class AutoConfigIT {

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AutoConfig.class));

    @Test
    void should_provide_embedding_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.voyage.embedding-model.api-key=" + System.getenv("VOYAGE_API_KEY"),
                        "langchain4j.voyage.embedding-model.model-name=" + VoyageEmbeddingModelName.VOYAGE_3_LITE.name(),
                        "langchain4j.voyage.embedding-model.log-requests=true",
                        "langchain4j.voyage.embedding-model.log-responses=true"
                )
                .run(context -> {

                    EmbeddingModel embeddingModel = context.getBean(EmbeddingModel.class);
                    assertThat(embeddingModel).isInstanceOf(VoyageEmbeddingModel.class);
                    assertThat(embeddingModel.embed("hi").content().dimension()).isEqualTo(VoyageEmbeddingModelName.VOYAGE_3_LITE.dimension());

                    assertThat(context.getBean(VoyageEmbeddingModel.class)).isSameAs(embeddingModel);
                });
    }

    @Test
    void should_provide_scoring_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.voyage.scoring-model.api-key=" + System.getenv("VOYAGE_API_KEY"),
                        "langchain4j.voyage.scoring-model.model-name=" + VoyageScoringModelName.RERANK_LITE_1.name(),
                        "langchain4j.voyage.scoring-model.log-requests=true",
                        "langchain4j.voyage.scoring-model.log-responses=true"
                )
                .run(context -> {

                    ScoringModel scoringModel = context.getBean(ScoringModel.class);
                    assertThat(scoringModel).isInstanceOf(VoyageScoringModel.class);


                    TextSegment catSegment = TextSegment.from("The Maine Coon is a large domesticated cat breed.");
                    TextSegment dogSegment = TextSegment.from("The sweet-faced, lovable Labrador Retriever is one of America's most popular dog breeds, year after year.");
                    List<TextSegment> segments = asList(catSegment, dogSegment);

                    String query = "tell me about dogs";
                    Response<List<Double>> response = scoringModel.scoreAll(segments, query);
                    List<Double> scores = response.content();
                    assertThat(scores).hasSize(2);
                    assertThat(scores.get(0)).isLessThan(scores.get(1));

                    assertThat(context.getBean(VoyageScoringModel.class)).isSameAs(scoringModel);
                });
    }
}
