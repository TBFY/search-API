package es.upm.oeg.tbfy.search.api.io;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import es.upm.oeg.librairy.api.facade.model.avro.*;
import es.upm.oeg.librairy.api.facade.model.rest.*;
import es.upm.oeg.librairy.api.facade.model.rest.Credentials;
import es.upm.oeg.librairy.api.facade.model.rest.DataFields;
import es.upm.oeg.librairy.api.facade.model.rest.DataSource;
import es.upm.oeg.librairy.api.facade.model.rest.DocReference;
import es.upm.oeg.librairy.api.facade.model.rest.ItemsRequest;
import es.upm.oeg.librairy.api.facade.model.rest.Reference;
import es.upm.oeg.librairy.api.facade.model.rest.TextReference;
import es.upm.oeg.tbfy.search.api.model.Filter;
import es.upm.oeg.tbfy.search.api.model.Item;
import es.upm.oeg.tbfy.search.api.service.LanguageService;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class LibrAIryClient {

    private static final Logger LOG = LoggerFactory.getLogger(LibrAIryClient.class);

    @Value("#{environment['SOLR_ENDPOINT']?:'${solr.endpoint}'}")
    String solrEndpoint;

    @Value("#{environment['LIBRAIRY_API_USERNAME']?:'${librairy.api.username}'}")
    String librairyUser;

    @Value("#{environment['LIBRAIRY_API_PASSWORD']?:'${librairy.api.password}'}")
    String librairyPassword;

    @Value("#{environment['LIBRAIRY_API_ENDPOINT']?:'${librairy.api.endpoint}'}")
    String librairyEndpoint;

    @Value("#{environment['MODEL_ENDPOINT']?:'${librairy.model.endpoint}'}")
    String modelEndpoint;

    @Autowired
    LanguageService languageService;


    static{
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
            CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
            Unirest.setHttpClient(httpclient);

        } catch (Exception e) {
            LOG.error("HTTP Error",e);
        }
    }


    public List<Item> getItemsById(String id, Filter filter){

        try {
            ItemsRequest request = new ItemsRequest();
            request.setSize(filter.getSize());

            Reference reference = new Reference();
            DocReference docReference = new DocReference();
            docReference.setId(id);
            reference.setDocument(docReference);
            request.setReference(reference);

            DataSource dataSource = new DataSource();
            DataFields dataFields = new DataFields();
            dataFields.setId("id");
            dataFields.setName("name_s");
            dataSource.setDataFields(dataFields);

            Credentials credentials = new Credentials();
            dataSource.setCredentials(credentials);

            List<String> filters = new ArrayList();
            if (filter.hasSource()) filters.add("source_s:"+filter.getSource());
            if (filter.hasLang()) filters.add("lang_s:"+filter.getLang());
            if (filter.hasName()) filters.add("name_s:*"+filter.getName()+"*");
            if (filter.hasText()) filters.add("txt_t:"+filter.getText());
            dataSource.setFilter(filters.stream().collect(Collectors.joining(" AND ")));

            dataSource.setFormat(ReaderFormat.SOLR_CORE);
            dataSource.setSize(-1l);
            dataSource.setOffset(0l);
            dataSource.setUrl(solrEndpoint);
            request.setDataSource(dataSource);

            HttpResponse<JsonNode> result = Unirest.post(librairyEndpoint + "/items").basicAuth(librairyUser, librairyPassword).body(request).asJson();

            return toItems(result);

        } catch (UnirestException e) {
            LOG.warn("Unexpected error", e);
            return Collections.emptyList();
        }

    }


    public List<Item> getItemsByText(String text, Filter filter){
        try {
            ItemsRequest request = new ItemsRequest();
            request.setSize(filter.getSize());

            Reference reference = new Reference();

            TextReference textReference = new TextReference();
            textReference.setContent(text);

            // detect language from text
            String lang = languageService.getLanguage(text);
            String model = modelEndpoint.replace("%%",lang);
            textReference.setModel(model);

            reference.setText(textReference);
            request.setReference(reference);

            DataSource dataSource = new DataSource();
            DataFields dataFields = new DataFields();
            dataFields.setId("id");
            dataFields.setName("name_s");
            dataSource.setDataFields(dataFields);

            Credentials credentials = new Credentials();
            dataSource.setCredentials(credentials);

            List<String> filters = new ArrayList();
            if (filter.hasSource()) filters.add("source_s:"+filter.getSource());
            if (filter.hasLang()) filters.add("lang_s:"+filter.getLang());
            if (filter.hasName()) filters.add("name_s:"+filter.getName());
            if (filter.hasText()) filters.add("text_t:"+filter.getText());
            dataSource.setFilter(filters.stream().collect(Collectors.joining(" AND ")));

            dataSource.setFormat(ReaderFormat.SOLR_CORE);
            dataSource.setSize(-1l);
            dataSource.setOffset(0l);
            dataSource.setUrl(solrEndpoint);
            request.setDataSource(dataSource);

            HttpResponse<JsonNode> result = Unirest.post(librairyEndpoint + "/items").basicAuth("librAIry", "l1brA1ry").body(request).asJson();

            return toItems(result);

        } catch (UnirestException e) {
            LOG.warn("Unexpected error", e);
            return Collections.emptyList();
        }
    }


    private List<Item> toItems(HttpResponse<JsonNode> result){
        List<Item> items = new ArrayList<>();

        if (result.getStatus() == 200){

            JSONArray list = result.getBody().getArray();
            for(int i=0;i<list.length();i++){
                JSONObject jsonItem = list.getJSONObject(i);
                Item item = new Item();
                item.setId(jsonItem.getString("id"));
                item.setName(jsonItem.getString("name"));
                item.setScore(jsonItem.getDouble("score"));

                items.add(item);
            }

        }
        return items;
    }
}
