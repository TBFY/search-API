package es.upm.oeg.tbfy.search.api.model;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class Filter {

    private static final Logger LOG = LoggerFactory.getLogger(Filter.class);

    private String id;

    private String lang;

    private String name;

    private String cursor;

    private Integer size;

    private String text;

    private String source;

    public Filter() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean hasId(){
        return !Strings.isNullOrEmpty(id);
    }

    public Boolean hasLang(){
        return !Strings.isNullOrEmpty(lang);
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public Boolean hasName(){
        return !Strings.isNullOrEmpty(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean hasCursor(){
        return !Strings.isNullOrEmpty(cursor);
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    public Boolean hasSize(){
        return size != null;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Boolean hasText(){
        return !Strings.isNullOrEmpty(text);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Boolean hasSource(){
        return !Strings.isNullOrEmpty(source);
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
