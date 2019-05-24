package es.upm.oeg.tbfy.search.api.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class QueryDocumentList {

    private static final Logger LOG = LoggerFactory.getLogger(QueryDocumentList.class);

    List<QueryDocument> documents;

    String cursor;

    Integer total;

    public QueryDocumentList() {
        documents = Collections.emptyList();
        cursor = "";
    }

    public QueryDocumentList(List<QueryDocument> documents, String cursor, Integer total) {
        this.documents = documents;
        this.cursor = cursor;
        this.total = total;
    }

    public List<QueryDocument> getDocuments() {
        return documents;
    }

    public String getCursor() {
        return cursor;
    }

    public Integer getTotal() {
        return total;
    }

    public boolean isEmpty(){
        return this.documents.isEmpty();
    }
}
