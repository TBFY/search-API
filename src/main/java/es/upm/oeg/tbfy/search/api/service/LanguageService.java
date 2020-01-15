package es.upm.oeg.tbfy.search.api.service;

import com.google.common.base.Strings;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.BuiltInLanguages;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class LanguageService {

    private static final Logger LOG = LoggerFactory.getLogger(LanguageService.class);

    private static final String DEFAULT_LANG = "en";

    private LanguageDetector languageDetector;
    private TextObjectFactory textObjectFactory;

    @PostConstruct
    public void setup() throws IOException {
        //load all languages:
        LanguageProfileReader langReader = new LanguageProfileReader();

        List<LanguageProfile> languageProfiles = new ArrayList<>();

        Iterator it = BuiltInLanguages.getLanguages().iterator();

        List<String> availableLangs = Arrays.asList(new String[]{"en","es","fr","it","pt"});
        while(it.hasNext()) {
            LdLocale locale = (LdLocale)it.next();
            if (availableLangs.contains(locale.getLanguage())) {
                LOG.info("language added: " + locale);
                languageProfiles.add(langReader.readBuiltIn(locale));
            }
        }


        //build language detector:
        this.languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                .minimalConfidence(0.85)
                .withProfiles(languageProfiles)
                .build();

        //create a text object factory
        this.textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText();
    }


    public Optional<String> getLanguage(String text){
        if (Strings.isNullOrEmpty(text)) return Optional.empty();

        Optional<String> language = Optional.empty();
        TextObject textObject   = textObjectFactory.forText(text);
        com.google.common.base.Optional<LdLocale> lang = languageDetector.detect(textObject);
        if (lang.isPresent()){
            language = Optional.of(lang.get().getLanguage());
        }
        return language;
    }

}
