package textrank.tagger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class LibrAIryTagger implements Tagger {

    private static final Logger LOG = LoggerFactory.getLogger(LibrAIryTagger.class);

    static{
        Unirest.setDefaultHeader("X-app-name", "tbfy-search-API");
        Unirest.setDefaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE);
        Unirest.setDefaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
//        jacksonObjectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        jacksonObjectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        Unirest.setObjectMapper(new ObjectMapper() {

            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return jacksonObjectMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object value) {
                try {
                    return jacksonObjectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        try {

            TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }

            } };


            SSLContext sslcontext = SSLContext.getInstance("SSL");
            sslcontext.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext);

            RequestConfig.Builder requestBuilder = RequestConfig.custom();
            requestBuilder.setConnectTimeout(20000);
            requestBuilder.setConnectionRequestTimeout(300000);
            requestBuilder.setSocketTimeout(300000);

            PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
            //Set the maximum number of connections in the pool
            connManager.setMaxTotal(100);

            HttpClientBuilder builder = HttpClientBuilder.create();
            builder.setConnectionManager(connManager);
            builder.setDefaultRequestConfig(requestBuilder.build());
            builder.setSSLSocketFactory(sslsf);

            CloseableHttpClient httpclient = builder.build();

            Unirest.setHttpClient(httpclient);

        } catch (Exception e) {
            LOG.error("HTTP Error",e);
        }
    }

    @Override
    public List<Term> seg(String text) {

        List<Term> terms = new ArrayList<Term>();

        JSONObject request = new JSONObject();
        request.put("filter","[]");
        request.put("multigrams","false");
        request.put("references","false");
        request.put("synset","false");
        request.put("text",text);

        try {
            HttpResponse<JsonNode> result = Unirest.post("http://librairy.linkeddata.es/nlp/annotations").body(request).asJson();

            if (result.getStatus() != 200){
                LOG.warn("Something went wrong: " + result.getStatus() + " - " + result.getBody());
                return new ArrayList<>();
            }

            JsonNode response = result.getBody();
            System.out.println(result.getBody());

            JSONArray tokens = response.getObject().getJSONArray("annotatedText");

            for(int i = 0; i<tokens.length();i++){
                JSONObject token = tokens.getJSONObject(i).getJSONObject("token");
                Term term = new Term();
                term.setPos(token.getString("pos"));
                term.setText(token.getString("target"));
                terms.add(term);
            }

            System.out.println(terms);

        } catch (UnirestException e) {
            LOG.error("Error getting tokens from text", e);
        }


        return terms;
    }
}
