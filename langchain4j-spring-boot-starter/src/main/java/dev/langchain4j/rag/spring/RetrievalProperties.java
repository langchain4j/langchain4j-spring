package dev.langchain4j.rag.spring;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class RetrievalProperties {

    Integer maxResults;
    Double minScore;
}
