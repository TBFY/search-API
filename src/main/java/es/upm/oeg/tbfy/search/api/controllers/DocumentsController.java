package es.upm.oeg.tbfy.search.api.controllers;

import es.upm.oeg.librairy.api.facade.model.rest.TopicsRequest;
import es.upm.oeg.tbfy.search.api.io.KGAPIClient;
import es.upm.oeg.tbfy.search.api.model.*;
import es.upm.oeg.tbfy.search.api.service.DateService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/documents")
@Api(tags="/documents", description = "public procurement and spending data")
public class DocumentsController {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentsController.class);

    @Autowired
    ItemsService itemsService;

    @Autowired
    DocumentsService documentsService;

    private ExecutorService executors;
    private AtomicInteger executions;

    @PostConstruct
    public void setup(){
        executors = Executors.newSingleThreadExecutor();
        executions = new AtomicInteger(0);
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

            Optional<InternalDocument> internalDocument = documentsService.getDocument(id, text);


            return internalDocument.isPresent()?
                    ResponseEntity.ok().body(internalDocument.get().toDocument()) :
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


    @ApiOperation(value = "add new documents", nickname = "postDocuments", response=String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "Accepted", response = String.class),
    })
    @RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity<es.upm.oeg.librairy.api.facade.model.rest.Task> create(
            @RequestBody DocumentsRequest request,
            @RequestHeader HttpHeaders headers)
    {
        String date = DateService.now();
        try {

            if (executions.get()>0){
                return new ResponseEntity(new es.upm.oeg.librairy.api.facade.model.rest.Task(date,"IN-PROGRESS","Task in execution"), HttpStatus.CONFLICT);
            }

            executors.submit(() -> {
                try{
                    executions.incrementAndGet();
                    documentsService.addAll();

                }catch (Exception e){
                    LOG.error("Error adding tenders",e);
                } finally {
                    executions.decrementAndGet();
                }
            });

            return new ResponseEntity(new es.upm.oeg.librairy.api.facade.model.rest.Task(date,"QUEUED","Task created"), HttpStatus.ACCEPTED);
        }catch (RuntimeException e){
            LOG.warn("Process error",e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            LOG.error("IO Error", e);
            return new ResponseEntity(new es.upm.oeg.librairy.api.facade.model.rest.Task(date, "REJECTED", "IO error"),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "document info", nickname = "createDocument", response=String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created", response = String.class),
    })
    @RequestMapping(value = "/{id:.+}", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<String> saveDocument(
            @ApiParam(value = "id", required = true) @PathVariable String id,
            @RequestBody DocumentBody document
    )  {
        try {

            LOG.info("saving document: " + document);
            Optional<InternalDocument> doc = documentsService.getDocument(id, false);

            if (doc.isPresent()) return new ResponseEntity<String>(HttpStatus.CONFLICT);

            InternalDocument internalDocument = InternalDocument.from(document,id);
            internalDocument.setFormat("api");
            boolean result = documentsService.add(internalDocument, true);

            if (!result) return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);

            return new ResponseEntity<String>(HttpStatus.CREATED);

        } catch (RuntimeException e) {
            LOG.error("Runtime Error: " + e.getMessage());
            return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            LOG.error("IO Error", e);
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
