package es.upm.oeg.tbfy.search.api.controllers;

import es.upm.oeg.tbfy.search.api.model.Filter;
import es.upm.oeg.tbfy.search.api.model.Item;
import es.upm.oeg.tbfy.search.api.model.ItemsRequest;
import es.upm.oeg.tbfy.search.api.service.ItemsService;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;

@RestController
@RequestMapping("/items")
@Api(tags="/items", description = "document suggestions")
public class ItemsServiceController {

    private static final Logger LOG = LoggerFactory.getLogger(ItemsServiceController.class);

    @Autowired
    ItemsService itemsService;

    @PostConstruct
    public void setup(){

    }

    @PreDestroy
    public void destroy(){

    }

    @ApiOperation(value = "similar docs to given a document", nickname = "getItemsById", response=Item.class, responseContainer = "list")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Item.class, responseContainer = "list"),
    })
    @RequestMapping(value = "/{id:.+}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<Item>> getItemsById(
            @ApiParam(value = "id", required = true) @PathVariable String id,
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "") String lang,
            @RequestParam(defaultValue = "") String text,
            @RequestParam(defaultValue = "") String source,
            @RequestParam(defaultValue = "10") Integer size
    )  {
        try {

            Filter filter = new Filter();
            filter.setName(name);
            filter.setLang(lang);
            filter.setText(text);
            filter.setSource(source);
            filter.setSize(size);

            List<Item> items = itemsService.getItemsById(id, filter);

            HttpHeaders responseHeaders = new HttpHeaders();

            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(items);
        } catch (RuntimeException e) {
            LOG.error("Runtime Error: " + e.getMessage());
            return new ResponseEntity<List<Item>>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            LOG.error("IO Error", e);
            return new ResponseEntity<List<Item>>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @ApiOperation(value = "similar docs to a given text", nickname = "getItemsByText", response=Item.class, responseContainer = "list")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok", response = Item.class, responseContainer = "list"),
    })
    @RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<Item>> getItemsByText(@RequestBody ItemsRequest request)  {
        try {
            if (!request.isValid()) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

            Filter filter = new Filter();
            filter.setName(request.getName());
            filter.setLang(request.getLang());
            filter.setText(request.getTerm());
            filter.setSource(request.getSource());
            filter.setSize(request.getSize());

            List<Item> items = itemsService.getItemsByText(request.getText(), filter);

            return ResponseEntity.ok()
                    .body(items);
        }catch (RuntimeException e){
            LOG.warn("Process error",e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            LOG.error("Unexpected Error", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
