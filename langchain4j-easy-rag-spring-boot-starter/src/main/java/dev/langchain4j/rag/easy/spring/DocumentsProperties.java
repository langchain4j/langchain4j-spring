package dev.langchain4j.rag.easy.spring;

import java.nio.file.Path;

public record DocumentsProperties(
    Path path,
    String glob,
    Boolean recursion
    ) {

}
