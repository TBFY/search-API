package es.upm.oeg.tbfy.search.api.io;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import es.upm.oeg.librairy.api.facade.model.avro.ReaderFormat;
import es.upm.oeg.librairy.api.facade.model.rest.*;
import es.upm.oeg.tbfy.search.api.executors.ParallelExecutor;
import es.upm.oeg.tbfy.search.api.model.Filter;
import es.upm.oeg.tbfy.search.api.model.Item;
import es.upm.oeg.tbfy.search.api.model.Organization;
import es.upm.oeg.tbfy.search.api.model.Tender;
import es.upm.oeg.tbfy.search.api.service.LanguageService;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class KGAPIClient {

    private static final Logger LOG = LoggerFactory.getLogger(KGAPIClient.class);

    @Value("#{environment['KG_API_ENDPOINT']?:'${kg.api.endpoint}'}")
    public String kgAPIEndpoint;


    private static final int availableProcessors = Runtime.getRuntime().availableProcessors();

    static{

        Unirest.setConcurrency(availableProcessors*4, availableProcessors);

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
                public X509Certificate[] getAcceptedIssuers() {
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
            requestBuilder.setConnectionRequestTimeout(600000);
            requestBuilder.setSocketTimeout(600000);

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

    private ParallelExecutor executor;

    @PostConstruct
    public void setup(){
        this.executor = new ParallelExecutor();
    }

    public List<Organization> getOrganizations(Integer size, Integer offset){

        try {

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("size",size);
            parameters.put("offset",offset);
            HttpResponse<JsonNode> result = Unirest.get(kgAPIEndpoint + "/organisation").queryString(parameters).asJson();

            List<Organization> organizations = new ArrayList<>();
            if (result.getStatus() == 200){

                JSONArray list = result.getBody().getArray();
                for(int i=0;i<list.length();i++){
                    final JSONObject jsonItem = list.getJSONObject(i);
                    Organization organization = new Organization();
                    organization.setId(jsonItem.getString("id"));
                    organization.setName(jsonItem.getString("legalName"));
                    organization.setJurisdiction(jsonItem.getString("jurisdiction"));
                    organizations.add(organization);
                }

            }
            return organizations;
        } catch (UnirestException e) {
            LOG.warn("Unexpected error", e);
            return Collections.emptyList();
        }

    }

    public List<Tender> getTenders(Integer size, Integer offset){

        Instant start = Instant.now();
        try {

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("size",size);
            parameters.put("offset",offset);
            HttpResponse<JsonNode> result = Unirest.get(kgAPIEndpoint + "/tender").queryString(parameters).asJson();

            List<Tender> tenders = new ArrayList<>();
            if (result.getStatus() == 200){

                JSONArray list = result.getBody().getArray();
                for(int i=0;i<list.length();i++){
                    final JSONObject jsonItem = list.getJSONObject(i);
                    Tender tender = new Tender();
                    tender.setId(jsonItem.getString("id"));
                    if (jsonItem.has("title")){
                        tender.setName(jsonItem.getString("title"));
                    }
                    if (jsonItem.has("description")){
                        tender.setText(jsonItem.getString("description"));
                    }
                    if (jsonItem.has("status")){
                        tender.setStatus(jsonItem.getString("status"));
                    }
                    if (jsonItem.has("_creationDate")){
                        tender.setCreationDate(jsonItem.getString("_creationDate"));
                    }

                    tenders.add(tender);
                }

            }else{
                LOG.warn("KG-API response status is: " + result.getStatus() + " for size: " + size + " and offset: " + offset);
            }
            return tenders;
        } catch (UnirestException e) {

            Instant end = Instant.now();

            String duration = ChronoUnit.HOURS.between(start, end) + "hours "
                    + ChronoUnit.MINUTES.between(start, end) % 60 + "min "
                    + (ChronoUnit.SECONDS.between(start, end) % 60) + "secs";

            LOG.warn(e.getMessage() + " error after: " + duration + " with size: " + size + " and offset: " + offset);
            return Collections.emptyList();
        }

    }

    public Optional<Tender> getTender(String id){
        Optional<Tender> rspTender = Optional.empty();
        try{

            HttpResponse<JsonNode> result = Unirest.get(kgAPIEndpoint + "/tender/"+id).asJson();


            if (result.getStatus() != 200){
                LOG.warn("HTTP ERROR getting tender by id: " + result.getStatus() + ": " + result.getStatusText());
                return rspTender;
            }

            JSONObject json = result.getBody().getObject();

            Tender tender = new Tender();
            tender.setId(id);
            tender.setName(json.getString("id"));
            tender.setText(json.getString("description"));

            rspTender = Optional.of(tender);
        }catch (UnirestException e){
            LOG.debug("Rest Exception: " + e.getMessage() + " from: " + id);
        }catch (Exception e){
            LOG.error("Unexpected error for tender: " + id,e);
        }
        return rspTender;
    }

    private List<Item> toItems(HttpResponse<JsonNode> result){
        List<Item> items = new ArrayList<>();

        if (result.getStatus() == 200){

            JSONArray list = result.getBody().getArray();
            for(int i=0;i<list.length();i++){
                JSONObject jsonItem = list.getJSONObject(i);
                Item item = new Item();
                item.setId(jsonItem.getString("id"));
                if (jsonItem.has("name")) item.setName(jsonItem.getString("name"));
                item.setScore(jsonItem.getDouble("score"));

                items.add(item);
            }

        }
        return items;
    }
}
