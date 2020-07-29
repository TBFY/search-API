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

    private LanguageDetector languageDetector;
    private TextObjectFactory textObjectFactory;

    private List<String> availableLangs = Arrays.asList(new String[]{"en","es","fr","it","pt"});

    @PostConstruct
    public void setup() throws IOException {
        //load all languages:
        List<LanguageProfile> languageProfiles = new LanguageProfileReader().readAllBuiltIn();



        this.languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                //.minimalConfidence(0.9)
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
        if (lang.isPresent() && availableLangs.contains(lang.get().getLanguage())){
            language = Optional.of(lang.get().getLanguage());
        }
        return language;
    }

    public static void main(String[] args) throws IOException {
        LanguageService service = new LanguageService();
        service.setup();

        String txt = "Do realizacji przedmiotu zamówienia wyznaczono następujące odcinki plaż:— kąpielisko morskie Dziwnów: od zejścia na plażę przy ul. Żeromskiego w stronę wschodnią do zejścia na plażę przy ul. Parkowej – 500 mb plaży,— kąpielisko morskie Dziwnów „Przymorze”: na wschód o ujścia rzeki Dziwnej przy zejściu na plażę od ul. Przymorze, długość linii brzegowej kąpieliska – 100 mb plaży od ujścia,— kąpielisko morskie Dziwnów „Spadochroniarzy Polskich”: przy zejściu na plażę od ulicy Spadochroniarzy Polskich po zachodniej stronie ujścia cieśniny Dziwnej, długość linii brzegowej kąpieliska – 100 mb plaży,— kąpielisko morskie Dziwnów „Słoneczne”: na zachód od wejścia na plażę od ul. Słonecznej – 100 mb.Ilość stanowisk ratowniczych: 1 stanowisko ratownicze na każde 100 mb plaży strzeżonej.Obsługa ratownicza stanowisk:— w okresie od  dnia 15 czerwca do dnia 30 czerwca – 9 osób (w tym 3 stanowiska ratownicze po 3 ratowników wodnych),— w okresie od dnia 1 lipca do dnia 31 sierpnia – 15 osób (w tym 5 stanowisk ratowniczych po 3 ratowników wodnych).Obsługa ratownicza stanowisk na kąpielisku morskim w Dziwnowie „Przymorze”, „Spadochroniarzy Polskich” i „Słoneczne”:— w okresie od dnia 1 lipca do dnia 31 sierpnia – 3 osoby (1 stanowisko ratownicze z 3 ratownikami wodnymi).Czas pracy ratowników w czasie realizacji umowy: każdego dnia kalendarzowego od godziny 9.00 do 17.00. Do realizacji przedmiotu zamówienia wyznaczono następujący odcinek plaży:— kąpielisko morskie Łukęcin „Bajkowa” o długości 100 mb na zachód od wejścia na plażę od ul. Bajkowej,— kąpielisko morskie Łukęcin „Spacerowa” o długości 100 mb na zachód od wejścia na plażę od ul. Spacerowej.Ilość stanowisk ratowniczych: 1 stanowisko ratownicze na każde 100 mb plaży strzeżonej.Obsługa ratownicza ww. stanowisk:— w okresie od dnia 15 czerwca do dnia 31 sierpnia – 3 osoby (1 stanowisko ratownicze z 3 ratownikami wodnymi).Czas pracy ratowników w czasie realizacji umowy: każdego dnia kalendarzowego od godziny 9.00 do 17.00. Do realizacji przedmiotu zamówienia wyznaczono następujący odcinek plaży:— kąpielisko morskie w Międzywodziu od ul. Westerplatte do ul. Szkolnej – 600 mb plaży.Ilość stanowisk ratowniczych: 1 stanowisko ratownicze na każde 100 mb plaży strzeżonej.Obsługa ratownicza stanowisk:— w okresie od dnia 15 czerwca do dnia 30 czerwca – 9 osób (w tym 3 stanowiska ratownicze po 3 ratowników wodnych),— w okresie od dnia 1 lipca do dnia 31 sierpnia – 18 osób (w tym 6 stanowisk ratowniczych po 3 ratowników wodnych).Czas pracy ratowników w czasie realizacji umowy: każdego dnia kalendarzowego od godziny 9.00 do 17.00.";

        LOG.info("Language identified: " + service.getLanguage(txt));
    }

}
