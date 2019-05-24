package es.upm.oeg.tbfy.search.api.io;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.upm.oeg.librairy.api.facade.model.avro.*;
import es.upm.oeg.librairy.api.facade.model.avro.Credentials;
import es.upm.oeg.librairy.api.facade.model.rest.*;
import es.upm.oeg.librairy.api.facade.model.rest.DataFields;
import es.upm.oeg.librairy.api.facade.model.rest.DataSource;
import es.upm.oeg.librairy.api.facade.model.rest.DocReference;
import es.upm.oeg.librairy.api.facade.model.rest.ItemsRequest;
import es.upm.oeg.librairy.api.facade.model.rest.Reference;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class LibrairyTypeTest {

    private static final Logger LOG = LoggerFactory.getLogger(LibrairyTypeTest.class);


    @Test
    public void mapping() throws JsonProcessingException {

        ItemsRequest request = new ItemsRequest();
        request.setSize(10);

        Reference reference = new Reference();
        DocReference docReference = new DocReference();
        docReference.setId("id");
        reference.setDocument(docReference);
        request.setReference(reference);

        DataSource dataSource = new DataSource();
        DataFields dataFields = new DataFields();
        dataFields.setId("id");
        dataFields.setName("name_s");
        dataSource.setDataFields(dataFields);

        List<String> filters = new ArrayList();
        filters.add("source_s:source");
        filters.add("lang_s:lang");
        filters.add("name_s:name");
        filters.add("text_t:text");
        dataSource.setFilter(filters.stream().collect(Collectors.joining(" AND ")));


        Credentials credentials = new Credentials();
        dataSource.setCredentials(credentials);

        dataSource.setFormat(ReaderFormat.SOLR_CORE);
        dataSource.setSize(-1l);
        dataSource.setOffset(0l);
        dataSource.setUrl("http://asdasd.asd");
        request.setDataSource(dataSource);

        ObjectMapper objectMapper = new ObjectMapper();

        LOG.info("json -> \n" + objectMapper.writeValueAsString(request));

    }
}
