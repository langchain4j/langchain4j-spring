package dev.langchain4j.store.embedding.elasticsearch.spring;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.InfoResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.Base64;
import java.util.Properties;

import static dev.langchain4j.internal.Utils.isNotNullOrBlank;

/**
 * For this test, because Elasticsearch container might not be fast to start,
 * devs could prefer having a cloud/local cluster running already.
 * We try first to reach the cloud/local cluster and if not available, then start
 * a container with Testcontainers.
 */
class ElasticsearchTestContainerHelper {

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchTestContainerHelper.class);

    RestClient restClient;
    ElasticsearchContainer elasticsearch;
    String certAsBase64;

    void startServices() throws IOException {
        String url = System.getenv("ELASTICSEARCH_URL");
        String apiKey = System.getenv("ELASTICSEARCH_API_KEY");
        String username = System.getenv("ELASTICSEARCH_USERNAME");
        String password = System.getenv("ELASTICSEARCH_PASSWORD");
        certAsBase64 = System.getenv("ELASTICSEARCH_CA_CERTIFICATE");

        if (isNotNullOrBlank(url)) {
            // If we have a cloud URL, we use that
            log.info("Starting Elasticsearch tests on [{}].", url);
            byte[] caCertificate = null;
            if (isNotNullOrBlank(certAsBase64)) {
                caCertificate = Base64.getDecoder().decode(certAsBase64);
            }
            restClient = getClient(url, apiKey, username, password, true, caCertificate);
        } else {
            Properties props = new Properties();
            props.load(ElasticsearchTestContainerHelper.class.getResourceAsStream("/version.properties"));
            String version = props.getProperty("elastic.version");

            // Start the container. This step might take some time...
            log.info("Starting testcontainers with Elasticsearch [{}].", version);
            elasticsearch = new ElasticsearchContainer(
                    DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch")
                            .withTag(version))
                    .withPassword(password);
            elasticsearch.start();
            byte[] certAsBytes = elasticsearch.copyFileFromContainer(
                    "/usr/share/elasticsearch/config/certs/http_ca.crt",
                    IOUtils::toByteArray);
            certAsBase64 = Base64.getEncoder().encodeToString(certAsBytes);
            restClient = getClient("https://" + elasticsearch.getHttpHostAddress(), null,
                    "elastic", password, false, certAsBytes);
        }
    }

    void stopServices() throws IOException {
        if (restClient != null) {
            restClient.close();
        }
        if (elasticsearch != null) {
            elasticsearch.stop();
        }
    }

    /**
     * Create an Elasticsearch Rest Client and test that it's running.
     *
     * @param address           the server url, like <a href="https://localhost:9200">https://localhost:9200</a>
     * @param apiKey            the API key if any. If null, we will be using login/password
     * @param username          the username to use if apiKey is not set.
     * @param password          the password to use if apiKey is not set.
     * @param checkCertificate  true if we want to check the certificate. If false, we won't check the certificate (tests only)
     * @param caCertificate     the SSL CA certificate to use if provided, otherwise we will use the system ones
     * @return the client
     */
    private RestClient getClient(String address, String apiKey, String username, String password,
                                 boolean checkCertificate, byte[] caCertificate) throws IOException {
        RestClient restClient = ElasticsearchClientHelper.getClient(address, apiKey, username, password,
                checkCertificate, caCertificate);
        InfoResponse info = new ElasticsearchClient(new RestClientTransport(restClient, new JacksonJsonpMapper())).info();
        log.info("Found Elasticsearch cluster version [{}] running at [{}].", info.version().number(), address);

        return restClient;
    }
}
