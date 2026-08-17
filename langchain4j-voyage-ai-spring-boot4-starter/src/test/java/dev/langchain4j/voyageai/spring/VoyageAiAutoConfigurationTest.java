package dev.langchain4j.voyageai.spring;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.scoring.ScoringModel;
import dev.langchain4j.model.voyageai.VoyageAiEmbeddingModel;
import dev.langchain4j.model.voyageai.VoyageAiEmbeddingModelName;
import dev.langchain4j.model.voyageai.VoyageAiScoringModel;
import dev.langchain4j.model.voyageai.VoyageAiScoringModelName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the auto-configuration creates the expected beans and that the properties declared by a user
 * actually reach the wire. The Voyage AI API is stubbed, so no API key and no network access are required.
 */
@WireMockTest
class VoyageAiAutoConfigurationTest {

    private static final String API_KEY = "test-api-key";
    private static final String EMBEDDINGS_PATH = "/v1/embeddings";
    private static final String RERANK_PATH = "/v1/rerank";

    private static final String EMBEDDING_RESPONSE =
            """
            {
              "object": "list",
              "model": "voyage-3-lite",
              "data": [{"object": "embedding", "index": 0, "embedding": [0.1, 0.2, 0.3]}],
              "usage": {"total_tokens": 1}
            }
            """;

    private static final String RERANK_RESPONSE =
            """
            {
              "object": "list",
              "model": "rerank-lite-1",
              "data": [
                {"object": "rerank", "index": 0, "relevance_score": 0.11},
                {"object": "rerank", "index": 1, "relevance_score": 0.97}
              ],
              "usage": {"total_tokens": 10}
            }
            """;

    private String baseUrl;

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(VoyageAiAutoConfiguration.class));

    @BeforeEach
    void setUp(WireMockRuntimeInfo wireMock) {
        baseUrl = wireMock.getHttpBaseUrl() + "/v1/";
    }

    @Test
    void should_provide_embedding_model() {
        WireMock.stubFor(post(urlEqualTo(EMBEDDINGS_PATH)).willReturn(okJson(EMBEDDING_RESPONSE)));

        contextRunner
                .withPropertyValues(
                        "langchain4j.voyage-ai.embedding-model.base-url=" + baseUrl,
                        "langchain4j.voyage-ai.embedding-model.api-key=" + API_KEY,
                        "langchain4j.voyage-ai.embedding-model.model-name="
                                + VoyageAiEmbeddingModelName.VOYAGE_3_LITE,
                        "langchain4j.voyage-ai.embedding-model.log-requests=true",
                        "langchain4j.voyage-ai.embedding-model.log-responses=true"
                )
                .run(context -> {

                    EmbeddingModel embeddingModel = context.getBean(EmbeddingModel.class);
                    assertThat(embeddingModel).isInstanceOf(VoyageAiEmbeddingModel.class);
                    assertThat(context.getBean(VoyageAiEmbeddingModel.class)).isSameAs(embeddingModel);

                    assertThat(embeddingModel.embed("hi").content().vector()).containsExactly(0.1f, 0.2f, 0.3f);

                    WireMock.verify(WireMock.postRequestedFor(urlEqualTo(EMBEDDINGS_PATH))
                            .withHeader("Authorization", equalTo("Bearer " + API_KEY))
                            .withRequestBody(matchingJsonPath(
                                    "$.model", equalTo(VoyageAiEmbeddingModelName.VOYAGE_3_LITE.toString()))));
                });
    }

    @Test
    void should_provide_scoring_model() {
        WireMock.stubFor(post(urlEqualTo(RERANK_PATH)).willReturn(okJson(RERANK_RESPONSE)));

        contextRunner
                .withPropertyValues(
                        "langchain4j.voyage-ai.scoring-model.base-url=" + baseUrl,
                        "langchain4j.voyage-ai.scoring-model.api-key=" + API_KEY,
                        "langchain4j.voyage-ai.scoring-model.model-name="
                                + VoyageAiScoringModelName.RERANK_LITE_1,
                        "langchain4j.voyage-ai.scoring-model.log-requests=true",
                        "langchain4j.voyage-ai.scoring-model.log-responses=true"
                )
                .run(context -> {

                    ScoringModel scoringModel = context.getBean(ScoringModel.class);
                    assertThat(scoringModel).isInstanceOf(VoyageAiScoringModel.class);
                    assertThat(context.getBean(VoyageAiScoringModel.class)).isSameAs(scoringModel);

                    TextSegment catSegment = TextSegment.from("The Maine Coon is a large domesticated cat breed.");
                    TextSegment dogSegment = TextSegment.from("The Labrador Retriever is a popular dog breed.");
                    List<TextSegment> segments = asList(catSegment, dogSegment);

                    String query = "tell me about dogs";
                    Response<List<Double>> response = scoringModel.scoreAll(segments, query);
                    List<Double> scores = response.content();
                    assertThat(scores).hasSize(2);
                    assertThat(scores.get(0)).isLessThan(scores.get(1));

                    WireMock.verify(WireMock.postRequestedFor(urlEqualTo(RERANK_PATH))
                            .withHeader("Authorization", equalTo("Bearer " + API_KEY))
                            .withRequestBody(matchingJsonPath(
                                    "$.model", equalTo(VoyageAiScoringModelName.RERANK_LITE_1.toString())))
                            .withRequestBody(matchingJsonPath("$.query", equalTo(query))));
                });
    }
}
