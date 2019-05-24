package es.upm.oeg.tbfy.search.api.service;


import es.upm.oeg.tbfy.search.api.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class})
@WebAppConfiguration
public class EvaluationIntTest {

    private static final Logger LOG = LoggerFactory.getLogger(EvaluationIntTest.class);

    @Test
    public void monolingual()  {

        LOG.info("hi");
    }

}