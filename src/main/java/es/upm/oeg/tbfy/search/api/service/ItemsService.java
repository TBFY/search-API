package es.upm.oeg.tbfy.search.api.service;

import es.upm.oeg.tbfy.search.api.io.LibrAIryClient;
import es.upm.oeg.tbfy.search.api.io.SolrSearcher;
import es.upm.oeg.tbfy.search.api.model.Filter;
import es.upm.oeg.tbfy.search.api.model.Item;
import es.upm.oeg.tbfy.search.api.model.QueryDocument;
import es.upm.oeg.tbfy.search.api.model.QueryDocumentList;
import org.apache.commons.lang3.StringUtils;
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
        //int remaining_words = -text.split(" ").length;
        int remaining_words = text.split(" ").length - 200;
        LOG.debug("Remaining Words: "+remaining_words);
        if (remaining_words < 0 ){
            List<String> keywords = librAIryClient.getKeywords(text);
            LOG.debug("Keywords: "+keywords);

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
                        LOG.debug("Added text: " + value);
                        int num_words = value.split(" ").length;
                        remaining_words += num_words;
                        extraText += ". " + value;
                    }
                }
            }

            // read from Solr and extend query text
            query_text += extraText;

        }

        LOG.debug("Final Query Text: " + query_text);


        // Validate the word frequencies
        Map<String, Integer> bow = librAIryClient.getBoW(query_text);

        List<Integer> values = new ArrayList<Integer>(bow.values());

        Integer maxFreq = Collections.max(values);
        Integer freqThreshold = 0;

        for (int i=0;i<maxFreq;i++){
            final Integer currentFreq = i;
            List<Integer> partialValues = values.stream().filter(a -> a > currentFreq).collect(Collectors.toList());
            double mean = partialValues.stream().mapToInt(Integer::intValue).average().getAsDouble();
            double median = partialValues.stream().sorted().skip(Math.max(0, ((partialValues.size() + 1) / 2) - 1))
                    .limit(1 + (1 + partialValues.size()) % 2).mapToInt(Integer::intValue).average().getAsDouble();
            double variance = partialValues.stream()
                    .map(j -> j - mean)
                    .map(j -> j*j)
                    .mapToDouble(j -> j).average().getAsDouble();


            boolean isOptimal = ((variance + median) > maxFreq) && ((maxFreq / 2) < mean);
            if (isOptimal){
                freqThreshold = currentFreq;
                break;
            }
        }

        LOG.info("Min frequency threshold is: " + freqThreshold);

        StringBuilder optimized_text = new StringBuilder();

        for (String key: bow.keySet()){
            Integer freq = bow.get(key);
            if (freq > freqThreshold){
                optimized_text.append(" ").append(StringUtils.repeat(key," ", freq));
            }
        }

        String extendedText = optimized_text.toString();

        return librAIryClient.getItemsByText(extendedText, filter);

    }

}
