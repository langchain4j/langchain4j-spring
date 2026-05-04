package dev.langchain4j.rag.easy.spring;

import java.nio.file.Path;

class DocumentsProperties {

    Path path;
    String glob;
    Boolean recursion;

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public String getGlob() {
        return glob;
    }

    public void setGlob(String glob) {
        this.glob = glob;
    }

    public Boolean getRecursion() {
        return recursion;
    }

    public void setRecursion(Boolean recursion) {
        this.recursion = recursion;
    }
}
