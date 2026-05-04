package dev.langchain4j.store.embedding.elasticsearch.spring;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayInputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import static dev.langchain4j.internal.Utils.isNullOrBlank;

/**
 * This class simply helps create a Rest Client instance
 */
class ElasticsearchClientHelper {

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchClientHelper.class);

    /**
     * Create an Elasticsearch Rest Client and test that it's running.
     *
     * @param address           the server url, like <a href="https://localhost:9200">https://localhost:9200</a>
     * @param apiKey            the API key if any. If null, we will be using login/password
     * @param username          the username to use if apiKey is not set.
     * @param password          the password to use if apiKey is not set.
     * @param checkCertificate  true if we want to check the certificate. If false, we won't check the certificate (tests only)
     * @param caCertificate     the SSL CA certificate to use if provided, otherwise we will use the system ones
     * @return the client instance
     */
    static RestClient getClient(String address, String apiKey, String username, String password,
                                boolean checkCertificate, byte[] caCertificate) {
        log.debug("Trying to connect to {}.", address);

        // Create the low-level client
        RestClientBuilder restClientBuilder = RestClient.builder(HttpHost.create(address));

        if (!isNullOrBlank(apiKey)) {
            restClientBuilder.setDefaultHeaders(new Header[]{
                    new BasicHeader("Authorization", "Apikey " + apiKey)
            });
        } else {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(username, password));
            restClientBuilder.setHttpClientConfigCallback(hcb -> hcb
                    .setDefaultCredentialsProvider(credentialsProvider)
                    .setSSLContext(getSSLContext(checkCertificate, caCertificate)));
        }

        return restClientBuilder.build();
    }

    /**
     * @param checkCertificate  true if we want to check the certificate. If false, we won't check the certificate (tests only)
     * @param caCertificate     the SSL CA certificate to use if provided, otherwise we will use the system ones
     * @return the SSL Context
     */
    private static SSLContext getSSLContext(boolean checkCertificate, byte[] caCertificate) {
        // If we don't want to check anything (not recommended for production)
        if (!checkCertificate) {
            return createTrustAllCertsContext();
        }

        // If we don't have a self-signed certificate
        if (caCertificate == null) {
            return null;
        }

        // If we have a self-signed certificate we need to check it against the fake Certificate Authority
        return createContextFromCaCert(caCertificate);
    }

    /**
     * Create an SSL Context from a Certificate
     * @param certificate Certificate provided as a byte array
     * @return the SSL Context
     */
    private static SSLContext createContextFromCaCert(byte[] certificate) {
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            Certificate trustedCa = factory.generateCertificate(
                    new ByteArrayInputStream(certificate)
            );
            KeyStore trustStore = KeyStore.getInstance("pkcs12");
            trustStore.load(null, null);
            trustStore.setCertificateEntry("ca", trustedCa);
            SSLContextBuilder sslContextBuilder = SSLContexts.custom().loadTrustMaterial(trustStore, null);
            return sslContextBuilder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
        @Override public void checkClientTrusted(X509Certificate[] chain, String authType) {}
        @Override public void checkServerTrusted(X509Certificate[] chain, String authType) {}
        @Override public X509Certificate[] getAcceptedIssuers() { return null; }
    }};

    private static SSLContext createTrustAllCertsContext() {
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            return sslContext;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("Can not create the SSLContext", e);
        }
    }
}
