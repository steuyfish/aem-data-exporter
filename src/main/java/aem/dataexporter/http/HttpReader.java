package aem.dataexporter.http;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reads the content from a url and returns the appropriate {@code byte[]} data.
 */
public class HttpReader {

    /**
     * Default timeout.
     */
    private static final int DEFAULT_TIMEOUT = 600000;
    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(HttpReader.class.getName());
    /**
     * {@code HttpClientContext} that contains the context for all http clients.
     */
    private HttpClientContext httpClientContext;
    /**
     * {@code HttpHost} to connect to.
     */
    private HttpHost httpHost;
    /**
     * Password to authenticate with.
     */
    private String password;
    /**
     * Username to authenticate with.
     */
    private String username;

    /**
     * Constructs a new {@code HttpReader}.
     *
     * @param newUsername Username to authenticate with.
     * @param newPassword Password to authenticate with.
     * @param hostname Name of the host to connect to.
     * @param port Number of the port to connect to.
     */
    public HttpReader(final String newUsername, final String newPassword, String hostname, String port) {
        this.username = newUsername;
        this.password = newPassword;
        httpHost = new HttpHost(hostname, Integer.parseInt(port), "http");
        AuthCache authCache = new BasicAuthCache();
        BasicScheme basicScheme = new BasicScheme();
        authCache.put(httpHost, basicScheme);
        httpClientContext = HttpClientContext.create();
        httpClientContext.setAuthCache(authCache);
    }

    /**
     * Returns the {@code byte[]} that represents the data retrieved from the provided url.
     *
     * @param url Url to retrieve data from.
     * @return {@code byte[]} that represents the data retrieved from the provided url.
     */
    public final byte[] getData(final String url) {
        CloseableHttpClient httpClient = getHttpClient();
        byte[] data = new byte[]{};
        try {
            HttpGet httpGet = new HttpGet(url);
            CloseableHttpResponse httpResponse = httpClient.execute(httpHost, httpGet, httpClientContext);
            data = EntityUtils.toByteArray(httpResponse.getEntity());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to retrieve content for url: [{0}]. {1}",
                    new String[]{url, e.getMessage()});
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Unable to retrieve content for url: [{0}]. {1}",
                        new String[]{url, e.getMessage()});
            }
        }
        return data;
    }

    /**
     * Gets and writes data to a stream using a buffer to handle memory issues when working with large amounts of data.
     *
     * @param url URL to retrieve data from.
     * @param out {@code OutputStream} to write data to.
     */
    public final void getDataAndWriteToOutputSteam(final String url, final OutputStream out) {
        CloseableHttpClient httpClient = getHttpClient();
        byte[] buffer = new byte[8192];
        try {
            HttpGet httpGet = new HttpGet(url);
            CloseableHttpResponse httpResponse = httpClient.execute(httpHost, httpGet, httpClientContext);
            HttpEntity entity = httpResponse.getEntity();
            InputStream in = entity.getContent();
            int count;
            while ((count = in.read(buffer)) > 0) {
                out.write(buffer, 0, count);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to retrieve content for url: [{0}]. {1}",
                    new String[]{url, e.getMessage()});
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Unable to retrieve content for url: [{0}]. {1}",
                        new String[]{url, e.getMessage()});
            }
        }

    }

    /**
     * Post method.
     *
     * @param url Url to post to.
     * @return True if the post was successful; otherwise false.
     */
    public final boolean post(final String url) {
        CloseableHttpClient httpClient = getHttpClient();
        int statusCode;
        try {
            HttpPost httpPost = new HttpPost(url);
            CloseableHttpResponse httpResponse = httpClient.execute(httpHost, httpPost, httpClientContext);
            statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                EntityUtils.consume(httpResponse.getEntity());
                LOGGER.log(Level.INFO, "Finished posting to {0}", new String[]{url});
                return true;
            }
            LOGGER.log(Level.SEVERE, "Unable to finish posting to {0}. HTTP status code {1}",
                    new String[]{url, String.valueOf(statusCode)});
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to post to url: [{0}]. {1}", new String[]{url, e.getMessage()});
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Unable to post to url: [{0}]. {1}", new String[]{url, e.getMessage()});
            }
        }
        return false;
    }

    /**
     * Post method and return response.
     *
     * @param url Url to post to.
     * @return response String or null if post fails.
     */
    public final String postAndGetResponseString(final String url) {
        CloseableHttpClient httpClient = getHttpClient();
        int statusCode;
        try {
            HttpPost httpPost = new HttpPost(url);
            CloseableHttpResponse httpResponse = httpClient.execute(httpHost, httpPost, httpClientContext);
            statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                HttpEntity entity = httpResponse.getEntity();
                String responseMessage = EntityUtils.toString(entity);
                EntityUtils.consume(httpResponse.getEntity());
                LOGGER.log(Level.INFO, "Finished posting to {0}", new String[]{url});
                return responseMessage;
            }
            LOGGER.log(Level.SEVERE, "Unable to finish posting to {0}. HTTP status code {1}",
                    new String[]{url, String.valueOf(statusCode)});
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to post to url: [{0}]. {1}", new String[]{url, e.getMessage()});
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Unable to post to url: [{0}]. {1}", new String[]{url, e.getMessage()});
            }
        }
        return null;
    }

    /**
     * Writes the file to the provided url.
     *
     * @param url Url to write data to.
     * @param filename Name of the file to write to provided url.
     * @param packageName Name of the package to upload.
     * @return True if the data was written successfully; otherwise false.
     */
    public final boolean writeData(final String url, final String filename, final String packageName) {
        CloseableHttpClient httpClient = getHttpClient();
        int statusCode;
        try {
            HttpPost httpPost = new HttpPost(url);
            FileBody fileBody = new FileBody(new File(filename));
            StringBody stringBody = new StringBody(packageName, ContentType.TEXT_PLAIN);
            HttpEntity httpEntity =
                    MultipartEntityBuilder.create().addPart("package", fileBody).addPart("name", stringBody).build();
            httpPost.setEntity(httpEntity);
            CloseableHttpResponse httpResponse = httpClient.execute(httpHost, httpPost, httpClientContext);
            statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                EntityUtils.consume(httpResponse.getEntity());
                LOGGER.log(Level.INFO, "Finished uploading [{0}] to {1}", new String[]{filename, url});
                return true;
            }
            LOGGER.log(Level.SEVERE, "Unable to finish uploading [{0}] to {1}: HTTP status code {2}",
                    new String[]{filename, url, String.valueOf(statusCode)});
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to upload content to url: [{0}]. {1}", new String[]{url, e.getMessage()});
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Unable to upload content to url: [{0}]. {1}",
                        new String[]{url, e.getMessage()});
            }
        }
        return false;
    }

    /**
     * Returns the {@code CloseableHttpClient} to use.
     *
     * @return {@code CloseableHttpClient} to use.
     */
    private CloseableHttpClient getHttpClient() {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM),
                new UsernamePasswordCredentials(username, password));
        RequestConfig requestConfig =
                RequestConfig.custom().setSocketTimeout(DEFAULT_TIMEOUT).setConnectTimeout(DEFAULT_TIMEOUT)
                        .setConnectionRequestTimeout(DEFAULT_TIMEOUT).build();
        return HttpClients.custom().setDefaultCredentialsProvider(credentialsProvider)
                .setDefaultRequestConfig(requestConfig).build();
    }

}
