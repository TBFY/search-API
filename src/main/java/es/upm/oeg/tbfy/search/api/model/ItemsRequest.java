package es.upm.oeg.tbfy.search.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemsRequest {

    private static final Logger LOG = LoggerFactory.getLogger(ItemsRequest.class);

    private String text;

    private String lang;

    private String source;

    private String name;

    private String terms;

    private Integer size;

    public ItemsRequest() {
    }

    public ItemsRequest(String text, String lang, String source, String name, String terms, Integer size) {
        this.text = text;
        this.lang = lang;
        this.source = source;
        this.name = name;
        this.terms = terms;
        this.size = size;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTerms() {
        return terms;
    }

    public void setTerms(String terms) {
        this.terms = terms;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public boolean isValid(){
        return !Strings.isNullOrEmpty(text);
    }
}
