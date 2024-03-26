package dev.langchain4j.rag.easy.spring;

import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;

@Getter
@Setter
class DocumentsProperties {

    Path path;
    String glob;
    Boolean recursion;
}
