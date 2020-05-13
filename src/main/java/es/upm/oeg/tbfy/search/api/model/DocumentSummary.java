package es.upm.oeg.tbfy.search.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentSummary {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentSummary.class);

    private String id;

    private String language;

    private String name;

    private String source;

    private String date;

    public DocumentSummary() {
    }

    public DocumentSummary(String id, String name, String language, String source, String date) {
        this.id = id;
        this.name = name;
        this.language = language;
        this.source = source;
        this.date = date;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DocumentSummary that = (DocumentSummary) o;

        if (!id.equals(that.id)) return false;
        if (language != null ? !language.equals(that.language) : that.language != null) return false;
        if (!name.equals(that.name)) return false;
        if (date != null ? !date.equals(that.date) : that.date != null) return false;
        return source != null ? source.equals(that.source) : that.source == null;

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (language != null ? language.hashCode() : 0);
        result = 31 * result + name.hashCode();
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        return result;
    }
}
