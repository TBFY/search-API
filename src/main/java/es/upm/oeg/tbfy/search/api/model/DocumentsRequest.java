package es.upm.oeg.tbfy.search.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentsRequest {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentsRequest.class);

    private Boolean organizations;

    private Boolean tenders;

    public DocumentsRequest() {
    }

    public DocumentsRequest(Boolean organizations, Boolean tenders) {
        this.organizations = organizations;
        this.tenders = tenders;
    }

    public Boolean getOrganizations() {
        return organizations;
    }

    public void setOrganizations(Boolean organizations) {
        this.organizations = organizations;
    }

    public Boolean getTenders() {
        return tenders;
    }

    public void setTenders(Boolean tenders) {
        this.tenders = tenders;
    }
}
