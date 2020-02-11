package es.upm.oeg.tbfy.search.api.service;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import es.upm.oeg.tbfy.search.api.executors.ParallelExecutor;
import es.upm.oeg.tbfy.search.api.io.KGAPIClient;
import es.upm.oeg.tbfy.search.api.io.LibrAIryClient;
import es.upm.oeg.tbfy.search.api.io.SolrSearcher;
import es.upm.oeg.tbfy.search.api.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class DocumentsService {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentsService.class);

    @Autowired
    KGAPIClient kgapiClient;

    @Autowired
    LibrAIryClient librAIryClient;

    @Autowired
    SolrSearcher solrSearcher;

    @Autowired
    LanguageService languageService;


    public Optional<InternalDocument> getDocument(String id, Boolean text){

        List<String> fields = new ArrayList<>(Arrays.asList("format_s","labels_t","lang_s","source_s","name_s","date_dt","topics0_t","topics1_t","topics2_t"));

        if (text) fields.add("txt_t");

        QueryDocumentList result = solrSearcher.getBy(Maps.newHashMap(ImmutableMap.of("id", id)), Optional.empty(), Optional.of(fields), 1, Optional.empty());

        if (result.isEmpty()) return Optional.empty();

        QueryDocument qd = result.getDocuments().get(0);
        Map<String, Object> qdd = qd.getData();


        InternalDocument document = new InternalDocument();
        document.setId(qd.getId());
        if (qdd.containsKey("format_s"))  document.setFormat(String.valueOf(qdd.get("format_s")));
        if (qdd.containsKey("lang_s"))  document.setLanguage(String.valueOf(qdd.get("lang_s")));
        if (qdd.containsKey("name_s"))  document.setName(String.valueOf(qdd.get("name_s")));
        if (qdd.containsKey("id"))  document.setId(String.valueOf(qdd.get("id")));
        if (qdd.containsKey("txt_t"))  document.setText(String.valueOf(qdd.get("txt_t")));
        if (qdd.containsKey("source_s"))  document.setSource(String.valueOf(qdd.get("source_s")));
        if (qdd.containsKey("date_dt"))  document.setDate(String.valueOf(qdd.get("date_dt")));


        //if (qdd.containsKey("labels_t"))  document.setLabels(Arrays.asList(String.valueOf(qdd.get("labels_t")).split(" ")));

        List<String> topics = new ArrayList<>();
        for (int i=0;i<3;i++){
            if (qdd.containsKey("topics"+i+"_t")){
                topics.addAll(Arrays.asList(String.valueOf(qdd.get("topics"+i+"_t")).split(" ")));
            }
        }

        if (!topics.isEmpty()) document.setTags(topics.stream().map(x -> x.replace(" ","_")).collect(Collectors.joining(" ")));

        return Optional.of(document);

    }

    public DocumentSummaryList getDocuments(Filter filter){
        List<String> fields = Arrays.asList("name_s","lang_s","source_s");

        Map<String, Object> queryParams = new HashMap<>();
        if (filter.hasId()) queryParams.put("id",filter.getId());
        if (filter.hasName()) queryParams.put("name_s","*"+filter.getName()+"*");
        if (filter.hasLang()) queryParams.put("lang_s",filter.getLang());
        if (filter.hasText()) queryParams.put("txt_t",filter.getText());
        if (filter.hasSource()) queryParams.put("source_s","*"+filter.getSource()+"*");


        Optional<String> cursor = filter.hasCursor()? Optional.of(filter.getCursor()) : Optional.empty();
        Integer size = filter.hasSize()? filter.getSize() : 10;


        QueryDocumentList result = solrSearcher.getBy(queryParams, Optional.empty(), Optional.of(fields), size, cursor);

        if (result.isEmpty()) return new DocumentSummaryList();

        List<QueryDocument> querDocs = result.getDocuments();
        List<DocumentSummary> documents = new ArrayList<>();

        for(QueryDocument qd: querDocs){
            Map<String, Object> qdd = qd.getData();

            DocumentSummary document = new DocumentSummary();
            document.setId(qd.getId());
            if (qdd.containsKey("lang_s"))  document.setLanguage(String.valueOf(qdd.get("lang_s")));
            if (qdd.containsKey("name_s"))  document.setName(String.valueOf(qdd.get("name_s")));
            if (qdd.containsKey("id"))  document.setId(String.valueOf(qdd.get("id")));
            if (qdd.containsKey("source_s"))  document.setSource(String.valueOf(qdd.get("source_s")));

            documents.add(document);
        }


        return new DocumentSummaryList(documents, result.getCursor(), result.getTotal());
    }

    public boolean add(InternalDocument document, Boolean commit){

        try{

            Optional<String> lang = languageService.getLanguage(document.getText());
            if (!lang.isPresent()) {
                LOG.warn("Language not supported");
                return false;
            }
            document.setLanguage(lang.get());

            Map<String, String> topics = librAIryClient.getTopics(document.getText(), document.getLanguage());
            if (!Strings.isNullOrEmpty(topics.get("0"))) document.setTopics0(topics.get("0"));
            if (!Strings.isNullOrEmpty(topics.get("1"))) document.setTopics1(topics.get("1"));
            if (!Strings.isNullOrEmpty(topics.get("2"))) document.setTopics2(topics.get("2"));

            solrSearcher.save(document);
            if (commit) solrSearcher.commit();
            LOG.info("Document '" + document.getId()+"' saved");

            return true;
        }catch (Exception e){
            LOG.error("Unexpected error",e);
            return false;
        }

    }

    public boolean addAll(){

        try{
            int counter = 0;
            int size = 50;
            int offset = 0;
            boolean completed = false;
            Instant start = Instant.now();

            //TODO remove solr index



            ParallelExecutor executor = new ParallelExecutor();

            while(!completed){
                // read tenders
                List<Tender> tenders = kgapiClient.getTenders(size, offset++);

                completed = tenders.size() < size;

                //TODO parallel
                for(Tender tender : tenders){
                    // save documents for each tender
                    final Tender tenderValue = tender;

                    executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                InternalDocument tenderDocument = new InternalDocument();

                                if (Strings.isNullOrEmpty(tenderValue.getText())) return;

                                tenderDocument.setId(tenderValue.getId());
                                tenderDocument.setFormat("kg");
                                tenderDocument.setName(tenderValue.getName());
                                tenderDocument.setText(tenderValue.getText());
                                tenderDocument.setDate(DateService.now());
                                tenderDocument.setSource("tender");

                                add(tenderDocument, false);
                            }catch (Exception e){
                                LOG.error("Unexpected error on tender: " + tenderValue.getId(), e);
                            }
                        }
                    });
                }
                counter += tenders.size();
            }

            executor.awaitTermination(5, TimeUnit.MINUTES);

            Instant end = Instant.now();

            String duration = ChronoUnit.HOURS.between(start, end) + "hours "
                    + ChronoUnit.MINUTES.between(start, end) % 60 + "min "
                    + (ChronoUnit.SECONDS.between(start, end) % 60) + "secs";

            LOG.info(counter + " documents added successfully in " + duration);

            return true;
        }catch (Exception e){
            LOG.error("Unexpected error",e);
            return false;
        }

    }

}
