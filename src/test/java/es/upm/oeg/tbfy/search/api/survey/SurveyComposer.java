package es.upm.oeg.tbfy.search.api.survey;

import com.google.common.collect.ImmutableMap;
import es.upm.oeg.tbfy.search.api.io.LibrAIryClient;
import es.upm.oeg.tbfy.search.api.io.SolrSearcher;
import es.upm.oeg.tbfy.search.api.model.Filter;
import es.upm.oeg.tbfy.search.api.model.Item;
import es.upm.oeg.tbfy.search.api.model.QueryDocument;
import es.upm.oeg.tbfy.search.api.model.QueryDocumentList;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class SurveyComposer {

    private static final Logger LOG = LoggerFactory.getLogger(SurveyComposer.class);

    private static SolrSearcher eurovocSearcher;
    private static SolrSearcher tbfySearcher;
    private static Map<String,String> eurovocCategories;
    private static String source           = "tender";
    private static int num_pairs           = 10;
    private static int min_txt_size        = 250;
    private static int max_txt_size        = 10000;
    private static List<Integer> topics_level = Lists.newArrayList(0,1,2);
    //List<String> languages  = Lists.newArrayList("en");
    private static List<String> languages  = Lists.newArrayList("en","es","fr", "it", "pt");

    static{
        eurovocSearcher = new SolrSearcher();
        eurovocSearcher.url = "http://librairy.linkeddata.es/data/eurovoc";
        eurovocSearcher.setup();

        tbfySearcher = new SolrSearcher();
        tbfySearcher.url = "http://librairy.linkeddata.es/data/tbfy";
        tbfySearcher.setup();

        eurovocCategories = new HashMap<>();
    }


    public static void main(String[] args) throws IOException {

        Map<String,Integer> langMap = new HashMap<>();
        languages.forEach(l -> langMap.put(l,1));

        //List<String> languages  = Lists.newArrayList("en");

        LibrAIryClient librAIryClient   = new LibrAIryClient();
        librAIryClient.solrEndpoint     = tbfySearcher.url;
        librAIryClient.librairyUser     = "oeg";
        librAIryClient.librairyPassword = "oeg2018";
        librAIryClient.librairyEndpoint = "http://librairy.linkeddata.es/api";

        BufferedWriter pairsWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Paths.get("pairs.csv").toFile(), false)));
        BufferedWriter docsWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Paths.get("documents.csv").toFile(), false)));


        pairsWriter.write("id1,id2,similarity,relation\n");
        docsWriter.write("id,language,l0,l1,l2,text\n");

        Set<String> documents = new TreeSet<>();

        for(String language: languages){

            Map<String, Object> queryParams = ImmutableMap.of();
            Optional<String> filterQuery = Optional.of("source_s:" + source + " AND lang_s:"+language + " AND size_i:["+min_txt_size+" TO "+max_txt_size+"]");
            Optional<java.util.List<String>> fields = Optional.of(Lists.newArrayList("name_s","txt_t","topics0_t","topics1_t","topics2_t"));
            Optional<String> cursor = Optional.empty();
            Integer counter = 0;

            while(counter < num_pairs){

                QueryDocumentList result = tbfySearcher.getBy(queryParams, filterQuery, fields, 100, cursor);
                if (result.isEmpty()) break;
                cursor = Optional.of(result.getCursor());

                for(QueryDocument document : result.getDocuments()){

                    String id1 = document.getId();

                    if (id1.contains("Tender_ocds")) continue;

                    Map<String, Object> data = document.getData();
                    Map<String,Integer> d1_topics = new HashMap<>();
                    for(Integer topic: topics_level){
                        d1_topics.put((String) data.get("topics"+topic+"_t"), topic);
                    }

                    String txt1 = (String) data.get("txt_t");

                    // Getting similar docs
                    Filter filter = new Filter();
                    filter.setId(id1);
                    //filter.setLang(language);
                    filter.setSize(1000);
                    filter.setSource(source);
                    List<Item> items = librAIryClient.getItemsById(id1, filter);

                    Map<Integer,String> pairs = new HashMap<>();

                    Integer index = 0;

                    for(Item item: items){

                        if (pairs.size() > 0 && !pairs.containsKey(1)) continue;

                        String id2 = item.getId();

                        if (id2.contains("Tender_ocds")) continue;

                        //if (!langMap.containsKey(item.getLang())) continue;

                        QueryDocumentList res = tbfySearcher.getBy(ImmutableMap.of("id", id2), Optional.empty(), fields, num_pairs, Optional.empty());

                        QueryDocument document2 = res.getDocuments().get(0);
                        Map<String, Object> data2 = document2.getData();

                        String txt2 = (String) data2.get("txt_t");

                        if (txt1.equalsIgnoreCase(txt2)) {
                            continue;
                        }


                        String t0 = (String) data2.get("topics0_t");
                        String t1 = (String) data2.get("topics1_t");
                        String t2 = (String) data2.get("topics2_t");

                        if (d1_topics.containsKey(t0) && d1_topics.containsKey(t1) && d1_topics.containsKey(t2)){
                            // 3 shared
                            if (d1_topics.get(t0) == 0){

                                if (d1_topics.get(t1) == 1){
                                    save(pairs, 1, id2);
                                }else{
                                    save(pairs, 2, id2);
                                }

                            }else if (d1_topics.get(t1) == 0){

                                if (d1_topics.get(t0) == 1){
                                    save(pairs, 3, id2);
                                }else{
                                    save(pairs, 4, id2);
                                }
                            }else{
                                if (d1_topics.get(t0) == 1){
                                    save(pairs, 5, id2);
                                }else{
                                    save(pairs, 6, id2);
                                }
                            }

                        } else if (d1_topics.containsKey(t0) && d1_topics.containsKey(t1)){
                            // 2 shared
                            if (d1_topics.get(t0) == 0) {

                                if (d1_topics.get(t1) == 1) {
                                    save(pairs, 7, id2);
                                } else {
                                    save(pairs, 13, id2);
                                }
                            }else if (d1_topics.get(t1) == 0){
                                if (d1_topics.get(t0) == 1) {
                                    save(pairs, 9, id2);
                                } else {
                                    save(pairs, 15, id2);
                                }
                            }else if (d1_topics.get(t0) == 1){
                                save(pairs, 19, id2);
                            }else{
                                save(pairs, 21, id2);
                            }

                        } else if (d1_topics.containsKey(t0) && d1_topics.containsKey(t2)){
                            // 2 shared
                            if (d1_topics.get(t0) == 0) {
                                if (d1_topics.get(t2) == 1) {
                                    save(pairs, 8, id2);
                                } else {
                                    save(pairs, 14, id2);
                                }
                            }else if (d1_topics.get(t2) == 0) {
                                if (d1_topics.get(t0) == 1) {
                                    save(pairs, 11, id2);
                                } else {
                                    save(pairs, 17, id2);
                                }
                            }else if (d1_topics.get(t0) == 1) {
                                save(pairs, 20, id2);
                            }else{
                                save(pairs, 23, id2);
                            }
                        } else if (d1_topics.containsKey(t1) && d1_topics.containsKey(t2)){
                            // 2 shared
                            if (d1_topics.get(t1) == 0) {
                                if (d1_topics.get(t2) == 1) {
                                    save(pairs, 10, id2);
                                } else {
                                    save(pairs, 16, id2);
                                }
                            }else if (d1_topics.get(t2) == 0){
                                if (d1_topics.get(t1) == 1) {
                                    save(pairs, 12, id2);
                                } else {
                                    save(pairs, 18, id2);
                                }
                            }else if (d1_topics.get(t1) == 1){
                                save(pairs, 22, id2);
                            }else{
                                save(pairs, 24, id2);
                            }

                        } else if (d1_topics.containsKey(t0)){
                            // 1 shared
                            switch (d1_topics.get(t0)){
                                case 0: save(pairs, 25, id2);
                                    break;
                                case 1: save(pairs, 28, id2);
                                    break;
                                case 2: save(pairs, 31, id2);
                                    break;
                            }

                        } else if (d1_topics.containsKey(t1)) {
                            // 1 shared
                            switch (d1_topics.get(t1)){
                                case 0: save(pairs, 26, id2);
                                    break;
                                case 1: save(pairs, 29, id2);
                                    break;
                                case 2: save(pairs, 32, id2);
                                    break;
                            }
                        } else if (d1_topics.containsKey(t2)) {
                            // 1 shared
                            switch (d1_topics.get(t2)){
                                case 0: save(pairs, 27, id2);
                                    break;
                                case 1: save(pairs, 30, id2);
                                    break;
                                case 2: save(pairs, 33, id2);
                                    break;
                            }
                        } else {
                            // 0 shared
                            save(pairs, 34, id2);
                        }
                    }

                    if (pairs.containsKey(1)) {

                        counter++;
                        documents.add(id1);

                        for(Map.Entry<Integer,String> entry : pairs.entrySet()){
                            documents.add(entry.getValue());
                            Integer relation = entry.getKey();
                            String similarity = "";
                            if (relation <7){
                                similarity = "HIGH";
                            } else if (relation < 25){
                                similarity = "MEDIUM";
                            } else if (relation < 34){
                                similarity = "LOW";
                            } else{
                                similarity = "NONE";
                            }
                            pairsWriter.write(id1 + "," + entry.getValue() + "," + similarity + "," + relation + "\n");

                        }

                        LOG.info("ID1: " + id1 + " -> " + pairs.size() + " similar items");
                        pairs.entrySet().forEach(entry -> LOG.info("R"+entry.getKey()+ ": " + entry.getValue()));

                        // Getting no similar docs

                        int maxPerLanguage = (pairs.size() / languages.size()) + 1;

                        for (String lang : languages){
                            int internalCounter = 0;
//                            String filterByTopics = d1_topics.entrySet().stream().map(entry -> "-topics" + entry.getValue() + "_t:" + entry.getKey()).collect(Collectors.joining(" AND "));
//
//                            filterByTopics += " AND source_s:" + source + " AND lang_s:" + lang + " AND size_i:["+min_txt_size+" TO "+max_txt_size+"]";

                            Map<String,Object> queryByTopics = new HashMap<>();
                            d1_topics.entrySet().forEach(t -> queryByTopics.put("-topics"+t.getValue()+"_t",t.getKey()));
                            queryByTopics.put("source_s",source);
                            queryByTopics.put("lang_s",lang);
                            queryByTopics.put("size_i","["+min_txt_size+" TO "+max_txt_size+"]");
                            QueryDocumentList r2 = tbfySearcher.getBy(queryByTopics, Optional.empty(), fields, 1000, Optional.empty());

                            for(QueryDocument document2 : r2.getDocuments()){

                                String id2 = document2.getId();

                                if (id2.contains("Tender_ocds")) continue;
                                Map<String, Object> data2 = document2.getData();

                                String txt2 = (String) data2.get("txt_t");

                                if (txt1.equalsIgnoreCase(txt2)){
                                    continue;
                                }

                                internalCounter++;

                                //save(pairs, 34, id2);
                                pairsWriter.write(id1 + "," + id2 + ",NONE,34\n");
                                documents.add(id2);
                                LOG.info("R34: " + id2);

                                if (internalCounter>maxPerLanguage) break;
                            }

                        }



                    }

                    if (counter > num_pairs) break;

                }
            }
        }

        // getting documents


        for(String id : documents){

            Map<String, Object> docQuery = ImmutableMap.of("id",id);;
            Optional<List<String>> docFields = Optional.of(Lists.newArrayList("name_s","txt_t","lang_s","topics0_t","topics1_t","topics2_t"));
            QueryDocumentList result = tbfySearcher.getBy(docQuery, Optional.empty(), docFields, 1, Optional.empty());

            if (result.isEmpty()) break;

            QueryDocument document = result.getDocuments().get(0);
            Map<String, Object> data = document.getData();
            String name = (String) data.get("name_s");
            String txt = (String) data.get("txt_t");
            String lang = (String) data.get("lang_s");

            String t0 = (String) data.get("topics0_t");
            String l0 = Arrays.stream(t0.split(" ")).map(t -> toEurovocLabel(t).replaceAll(" ", "_")).collect(Collectors.joining(" "));

            String t1 = (String) data.get("topics1_t");
            String l1 = Arrays.stream(t1.split(" ")).map(t -> toEurovocLabel(t).replaceAll(" ", "_")).collect(Collectors.joining(" "));

            String t2 = (String) data.get("topics2_t");
            String l2 = Arrays.stream(t2.split(" ")).map(t -> toEurovocLabel(t).replaceAll(" ", "_")).collect(Collectors.joining(" "));

            docsWriter.write(id +","+lang+ ","+l0 + ","+l1+"," + l2 + ",\""+txt+"\"\n");
            LOG.info("doc '" + id + "' added to csv..");

        }


        pairsWriter.close();
        docsWriter.close();
    }

    private static String toEurovocLabel(String id){
        if (eurovocCategories.containsKey(id)) return eurovocCategories.get(id);
        Map<String, Object> docQuery = ImmutableMap.of("id",id);;
        Optional<List<String>> docFields = Optional.of(Lists.newArrayList("en_s"));
        QueryDocumentList result = eurovocSearcher.getBy(docQuery, Optional.empty(), docFields, 1, Optional.empty());
        QueryDocument document = result.getDocuments().get(0);
        String label = (String) document.getData().get("en_s");
        eurovocCategories.put(id,label);
        return label;
    }

    private static void save(Map<Integer,String> pairs, Integer relation, String id){
        if (!pairs.containsKey(relation)) pairs.put(relation, id);
    }

}
