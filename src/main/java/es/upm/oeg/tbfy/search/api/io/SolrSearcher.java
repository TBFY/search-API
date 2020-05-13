package es.upm.oeg.tbfy.search.api.io;

import com.google.common.base.Strings;
import es.upm.oeg.tbfy.search.api.model.Document;
import es.upm.oeg.tbfy.search.api.model.InternalDocument;
import es.upm.oeg.tbfy.search.api.model.QueryDocument;
import es.upm.oeg.tbfy.search.api.model.QueryDocumentList;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CursorMarkParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class SolrSearcher {

    private static final Logger LOG = LoggerFactory.getLogger(SolrSearcher.class);

    private  SolrClient solrClient;

    @Value("#{environment['SOLR_ENDPOINT']?:'${solr.endpoint}'}")
    public String url;

    @Value("#{environment['SOLR_USERNAME']?:'${solr.username}'}")
    public String username;

    @Value("#{environment['SOLR_PASSWORD']?:'${solr.password}'}")
    public String password;

    public AtomicInteger counter;

    @PostConstruct
    public void setup(){

        HttpClientBuilder client = HttpClientBuilder.create();

        if (!Strings.isNullOrEmpty(username) && !Strings.isNullOrEmpty(password)){
            CredentialsProvider provider = new BasicCredentialsProvider();
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("myusername","mypassword");
            provider.setCredentials(AuthScope.ANY, credentials);

            client.setDefaultCredentialsProvider(provider);
        }

        this.counter        = new AtomicInteger(0);

        this.solrClient     = new HttpSolrClient.Builder(url).withHttpClient(client.build()).build();
    }

    public QueryDocumentList getBy(Map<String, Object> queryParams, Optional<String> filterQuery, Optional<List<String>> fields, Integer max, Optional<String> cursor) {

        SolrQuery refQuery = new SolrQuery();
        try {
            refQuery.setRows(max);
            refQuery.addField("id");
            refQuery.addField("score");
            if (cursor.isPresent()){
                refQuery.setSort("id", SolrQuery.ORDER.asc);
                refQuery.set(CursorMarkParams.CURSOR_MARK_PARAM, cursor.get());
            }else{
                refQuery.setSort("id", SolrQuery.ORDER.asc);
                refQuery.set(CursorMarkParams.CURSOR_MARK_PARAM, CursorMarkParams.CURSOR_MARK_START);
            }
            refQuery.addField("name");
            if (fields.isPresent()){
                fields.get().forEach(f -> refQuery.addField(f));
            }

            String query = "*:*";

            //queryParams.put("txt_t","[* TO *]");

            if (!queryParams.isEmpty()){
                query = queryParams.entrySet().stream().map(e -> e.getKey()+":"+e.getValue()).collect(Collectors.joining(" AND "));
            }

            refQuery.setQuery(query);

            if (filterQuery.isPresent()) refQuery.addFilterQuery(filterQuery.get());

            QueryResponse rsp = solrClient.query(refQuery);

            String nextCursor = rsp.getNextCursorMark();

            if (rsp.getResults().isEmpty()){
                LOG.info("No found documents by query: " + query + " and filter: " + filterQuery);
                return new QueryDocumentList();
            }

            SolrDocumentList results = rsp.getResults();

            long total = rsp.getResults().getNumFound();

            QueryDocumentList qdl = new QueryDocumentList(toQueryDocuments(results,fields), nextCursor, Long.valueOf(total).intValue());
            return qdl;

        } catch (SolrServerException e) {
            LOG.error("Error reading solr core",e);
            return new QueryDocumentList();
        } catch (IOException e) {
            LOG.error("Error connecting to solr server",e);
            return new QueryDocumentList();
        } catch (Exception e){
            LOG.error("Unexpected query error",e);
            return new QueryDocumentList();
        }


    }

    private List<QueryDocument> toQueryDocuments(SolrDocumentList solrDocumentList, Optional<List<String>> fields){
        List<QueryDocument> queryDocuments = new ArrayList<>();

        for (SolrDocument result : solrDocumentList){
            QueryDocument queryDocument = new QueryDocument();
            queryDocument.setId((String)result.getFieldValue("id"));
            queryDocument.setScore(((Float)result.getFieldValue("score")).doubleValue());
            Map<String,Object> data = new HashMap<>();
            if (fields.isPresent()){
                for(String field : fields.get()){
                    Object value = result.getFieldValue(field);
                    if (value == null ) continue;
                    data.put(field,value );
                }
            }
            queryDocument.setData(data);
            queryDocuments.add(queryDocument);
        }

        return queryDocuments;
    }


    public Boolean exists(String id) {

        SolrQuery refQuery = new SolrQuery();
        try {
            refQuery.setRows(0);

            String query = "id:"+id;

            refQuery.setQuery(query);

            QueryResponse rsp = solrClient.query(refQuery);

            long total = rsp.getResults().getNumFound();

            return total > 0;

        } catch (SolrServerException e) {
            LOG.error("Error reading solr core",e);
            return false;
        } catch (IOException e) {
            LOG.error("Error connecting to solr server",e);
            return false;
        } catch (Exception e){
            LOG.error("Unexpected query error",e);
            return false;
        }


    }

    public boolean save(InternalDocument doc){

        try {
            SolrInputDocument document = new SolrInputDocument();
            document.addField("id",doc.getId());
            document.addField("name_s",doc.getName());
            document.addField("txt_t",doc.getText());
            document.addField("size_i", Strings.isNullOrEmpty(doc.getText())? 0 : doc.getText().length());
            document.addField("labels_t",doc.getTags());
            document.addField("format_s",doc.getFormat());
            document.addField("lang_s",doc.getLanguage());
            document.addField("source_s",doc.getSource());
            document.addField("date_dt",doc.getDate());

            if (!Strings.isNullOrEmpty(doc.getTopics0())) document.addField("topics0_t",doc.getTopics0());
            if (!Strings.isNullOrEmpty(doc.getTopics1())) document.addField("topics1_t",doc.getTopics1());
            if (!Strings.isNullOrEmpty(doc.getTopics2())) document.addField("topics2_t",doc.getTopics2());

            solrClient.add(document);

            if (counter.incrementAndGet() % 100 == 0) {
                LOG.info(counter.get() + " documents saved");
                solrClient.commit();
            }

        } catch (Exception e) {
            LOG.error("Unexpected error", e);
            return false;
        }

        return true;
    }


    public void commit (){
        try {
            solrClient.commit();
        } catch (Exception e) {
            LOG.error("Unexpected error saving documents",e);
        }
    }
}
