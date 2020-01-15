package es.upm.oeg.tbfy.search.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InternalDocument {

    private static final Logger LOG = LoggerFactory.getLogger(InternalDocument.class);

    private String id;

    private String name;

    private String text;

    private String format;

    private String language;

    private String source;

    private String date;

    private String tags;

    private String topics0;

    private String topics1;

    private String topics2;

    public InternalDocument() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getTopics0() {
        return topics0;
    }

    public void setTopics0(String topics0) {
        this.topics0 = topics0;
    }

    public String getTopics1() {
        return topics1;
    }

    public void setTopics1(String topics1) {
        this.topics1 = topics1;
    }

    public String getTopics2() {
        return topics2;
    }

    public void setTopics2(String topics2) {
        this.topics2 = topics2;
    }

    public static InternalDocument from(Document document){
        InternalDocument internalDocument = new InternalDocument();
        internalDocument.setId(document.getId());
        internalDocument.setName(document.getName());
        internalDocument.setSource(document.getSource());
        internalDocument.setText(document.getText());
        internalDocument.setDate(document.getDate());
        internalDocument.setTags(document.getTags());
        return internalDocument;
    }

    public static InternalDocument from(DocumentBody document, String id){
        InternalDocument internalDocument = new InternalDocument();
        internalDocument.setId(id);
        internalDocument.setName(document.getName());
        internalDocument.setSource(document.getSource());
        internalDocument.setText(document.getText());
        internalDocument.setDate(document.getDate());
        internalDocument.setTags(document.getTags());
        return internalDocument;
    }

    public Document toDocument(){
        Document document = new Document();
        document.setId(this.id);
        document.setName(this.name);
        document.setSource(this.source);
        document.setDate(this.date);
        document.setText(this.text);
        document.setTags(this.tags);
        return document;
    }
}
