package es.upm.oeg.tbfy.search.api.controllers;

import es.upm.oeg.tbfy.search.api.model.*;
import es.upm.oeg.tbfy.search.api.service.DocumentsService;
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
import java.util.Optional;

@RestController
@RequestMapping("/documents")
@Api(tags="/documents", description = "public procurement and spending data")
public class DocumentsController {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentsController.class);

    @Autowired
    ItemsService itemsService;

    @Autowired
    DocumentsService documentsService;

    @PostConstruct
    public void setup(){

    }

    @PreDestroy
    public void destroy(){

    }


    @ApiOperation(value = "list of documents", nickname = "getDocuments", response=DocumentSummary.class, responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = DocumentSummary.class, responseContainer = "List"),
    })
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<DocumentSummary>> getDocuments(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "") String lang,
            @RequestParam(defaultValue = "") String text,
            @RequestParam(defaultValue = "") String source,
            @RequestParam(defaultValue = "") String cursor,
            @RequestParam(defaultValue = "10") Integer size
    )  {
        try {

            Filter filter = new Filter();
            filter.setName(name);
            filter.setLang(lang);
            filter.setText(text);
            filter.setSource(source);
            filter.setCursor(cursor);
            filter.setSize(size);

            DocumentSummaryList documents = documentsService.getDocuments(filter);

            // add next cursor on header
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("x-cursor", documents.getCursor());
            responseHeaders.set("x-total", String.valueOf(documents.getTotal()));

            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(documents.getDocuments());

        } catch (RuntimeException e) {
            LOG.error("Internal Error", e);
            return new ResponseEntity<List<DocumentSummary>>(HttpStatus.FAILED_DEPENDENCY);
        } catch (Exception e) {
            LOG.error("IO Error", e);
            return new ResponseEntity<List<DocumentSummary>>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "document info", nickname = "getDocument", response=Document.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Document.class),
    })
    @RequestMapping(value = "/{id:.+}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Document> getDocument(
            @ApiParam(value = "id", required = true) @PathVariable String id,
            @RequestParam(defaultValue = "True") Boolean text
    )  {
        try {

            Optional<Document> document = documentsService.getDocument(id, text);

            return document.isPresent()?
                    ResponseEntity.ok().body(document.get()) :
                    ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            LOG.error("Runtime Error: " + e.getMessage());
            return new ResponseEntity<Document>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            LOG.error("IO Error", e);
            return new ResponseEntity<Document>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @ApiOperation(value = "similar documents", nickname = "getItems", response=Item.class, responseContainer = "list")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Item.class, responseContainer = "list"),
    })
    @RequestMapping(value = "/{id:.+}/items", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<Item>> getItems(
            @ApiParam(value = "id", required = true) @PathVariable String id,
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "") String lang,
            @RequestParam(defaultValue = "") String terms,
            @RequestParam(defaultValue = "") String source,
            @RequestParam(defaultValue = "10") Integer size
    )  {
        try {

            Filter filter = new Filter();
            filter.setName(name);
            filter.setLang(lang);
            filter.setText(terms);
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

}
