package es.upm.oeg.tbfy.search.api.service;

import es.upm.oeg.tbfy.search.api.io.LibrAIryClient;
import es.upm.oeg.tbfy.search.api.io.SolrSearcher;
import es.upm.oeg.tbfy.search.api.model.Filter;
import es.upm.oeg.tbfy.search.api.model.Item;
import es.upm.oeg.tbfy.search.api.model.QueryDocument;
import es.upm.oeg.tbfy.search.api.model.QueryDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class ItemsService {

    private static final Logger LOG = LoggerFactory.getLogger(ItemsService.class);


    @Autowired
    LibrAIryClient librAIryClient;

    @Autowired
    SolrSearcher solrSearcher;


    public List<Item> getItemsById(String id, Filter filter){

        return librAIryClient.getItemsById(id, filter);


    }

    public List<Item> getItemsByText(String text, Filter filter){

        String query_text = text;

        // If text is short, we need to improve it by adding extra content
        int remaining_words = text.split(" ").length - 200;
        if (remaining_words < 0 ){
            List<String> keywords = librAIryClient.getKeywords(text);
            //System.out.println(keywords);

            Map<String,Object> queryParams = new HashMap<>();
            String kw_query = keywords.stream().map(e -> "txt_t:\"" + e + "\"").collect(Collectors.joining(" OR "));
            queryParams.put("raw_query",kw_query);

            QueryDocumentList result = solrSearcher.getBy(queryParams, Optional.of("source_s:tender"), Optional.of(Arrays.asList("name_s")), 20, Optional.empty());

            String extraText = "";
            if (!result.isEmpty()){
                for (QueryDocument qd : result.getDocuments()){
                    Map<String, Object> data = qd.getData();
                    if (remaining_words < 0 && data.containsKey("name_s")){
                        String value = (String) data.get("name_s");
                        int num_words = value.split(" ").length;
                        remaining_words += num_words;
                        extraText += ". " + value;
                    }
                }
            }

            // read from Solr and extend query text

            query_text += extraText;

        }

        //System.out.println(query_text);
        return librAIryClient.getItemsByText(query_text, filter);

    }

}
