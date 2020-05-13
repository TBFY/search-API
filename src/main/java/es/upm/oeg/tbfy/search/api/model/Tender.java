package es.upm.oeg.tbfy.search.api.model;

import es.upm.oeg.tbfy.search.api.service.DateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class Tender {

    private static final Logger LOG = LoggerFactory.getLogger(Tender.class);

    private String id;

    private String name;

    private String text;

    private String status;

    private String creationDate;

    public Tender() {
        this.id = "";
        this.name = "";
        this.text = "";
        this.status = "";
        this.creationDate = DateService.now();
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }
}
