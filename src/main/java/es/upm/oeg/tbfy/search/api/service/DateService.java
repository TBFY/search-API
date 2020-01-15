package es.upm.oeg.tbfy.search.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class DateService {

    private static final Logger LOG = LoggerFactory.getLogger(DateService.class);

    private static TimeZone tz = TimeZone.getTimeZone("UTC");
    private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");


    static{
        df.setTimeZone(tz);
    }

    public static String now(){
        return df.format(new Date());
    }

    public static String from(Long timestamp){
        return df.format(timestamp);
    }


}
