package dev.langchain4j.spring.it;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The release deploys every module of the build, and the Central publishing plugin is applied to all of them. The
 * applications under integration-tests/ are kept off Maven Central only by the excludeArtifacts list in the root
 * pom.xml, so a module that is added here but not listed there would be published.
 */
class IntegrationTestsAreNotPublishedTest {

    private static final Path INTEGRATION_TESTS = Path.of("..").toAbsolutePath().normalize();

    @Test
    void every_integration_test_module_should_be_excluded_from_publishing() throws Exception {
        Document integrationTestsPom = parse(INTEGRATION_TESTS.resolve("pom.xml"));
        Document rootPom = parse(INTEGRATION_TESTS.resolveSibling("pom.xml"));

        List<String> artifactIds = new ArrayList<>();
        artifactIds.add(artifactId(integrationTestsPom));
        for (String module : texts(integrationTestsPom.getElementsByTagName("module"))) {
            artifactIds.add(artifactId(parse(INTEGRATION_TESTS.resolve(module).resolve("pom.xml"))));
        }

        assertThat(texts(rootPom.getElementsByTagName("excludeArtifact"))).containsAll(artifactIds);
    }

    private static Document parse(Path pom) throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pom.toFile());
    }

    private static String artifactId(Document pom) {
        NodeList children = pom.getDocumentElement().getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if ("artifactId".equals(children.item(i).getNodeName())) {
                return children.item(i).getTextContent().trim();
            }
        }
        throw new IllegalStateException("no artifactId in " + pom.getDocumentURI());
    }

    private static List<String> texts(NodeList nodes) {
        List<String> texts = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            texts.add(nodes.item(i).getTextContent().trim());
        }
        return texts;
    }
}
