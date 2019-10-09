package es.upm.oeg.tbfy.search.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.solr.SolrAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, SolrAutoConfiguration.class})
@ComponentScan(basePackages = {"es.upm.oeg.tbfy.search.api","es.upm.oeg.librairy.service"})
public class Application  {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    // main method
    public static void main(String[] args) {

        new SpringApplication(Application.class).run(args);
    }

}
