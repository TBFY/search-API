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

    public DocumentSummary() {
    }

    public DocumentSummary(String id, String name, String language, String source) {
        this.id = id;
        this.name = name;
        this.language = language;
        this.source = source;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DocumentSummary that = (DocumentSummary) o;

        if (!id.equals(that.id)) return false;
        if (language != null ? !language.equals(that.language) : that.language != null) return false;
        if (!name.equals(that.name)) return false;
        return source != null ? source.equals(that.source) : that.source == null;

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (language != null ? language.hashCode() : 0);
        result = 31 * result + name.hashCode();
        result = 31 * result + (source != null ? source.hashCode() : 0);
        return result;
    }
}
