package es.upm.oeg.tbfy.search.api.service;

import es.upm.oeg.tbfy.search.api.io.LibrAIryClient;
import es.upm.oeg.tbfy.search.api.model.Filter;
import es.upm.oeg.tbfy.search.api.model.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class ItemsService {

    private static final Logger LOG = LoggerFactory.getLogger(ItemsService.class);


    @Autowired
    LibrAIryClient librAIryClient;


    public List<Item> getItemsById(String id, Filter filter){

        return librAIryClient.getItemsById(id, filter);


    }

    public List<Item> getItemsByText(String text, Filter filter){

        return librAIryClient.getItemsByText(text, filter);

    }

}
