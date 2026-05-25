package com.vodafone.tobi2.service.translation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "tobi2.translation.provider", havingValue = "mock", matchIfMissing = true)
public class MockTranslationService implements TranslationService {

    private static final Logger log = LoggerFactory.getLogger(MockTranslationService.class);

    @Override
    public String detectLanguage(String text) {
        String m = normalize(text);
        String lang = detectFromKeywords(m);
        log.debug("MockTranslation: detected '{}' for text: {}", lang, truncate(text));
        return lang;
    }

    @Override
    public String translate(String text, String targetLanguage) {
        String detected = detectLanguage(text);
        return translate(text, detected, targetLanguage);
    }

    @Override
    public String translate(String text, String sourceLanguage, String targetLanguage) {
        if (sourceLanguage.equals(targetLanguage)) {
            return text;
        }
        String translated = mapKeywords(text, sourceLanguage, targetLanguage);
        log.debug("MockTranslation: {} -> {}: {}", sourceLanguage, targetLanguage, truncate(translated));
        return translated;
    }

    private String mapKeywords(String text, String sourceLang, String targetLang) {
        if ("en".equals(targetLang)) {
            String m = normalize(text);
            if (matchesAny(m, "solde", "soldi", "saldo", "guthaben", "balance", "kredi", "saldo")) return "my balance";
            if (matchesAny(m, "fattura", "facture", "rechnung", "factura", "fatur", "bill", "cuenta")) return "my bill";
            if (matchesAny(m, "ricarica", "recharge", "aufladen", "recarga", "mbush", "top")) return "top up";
            if (matchesAny(m, "internet", "rete", "reseau", "netz", "red", "wifi", "rrjet")) return "internet not working";
            if (matchesAny(m, "piano", "forfait", "tarif", "plan", "oferta", "offre", "angebot", "offerta")) return "what plans do you have";
            if (matchesAny(m, "agente", "agent", "operateur", "mitarbeiter", "agjent", "operator")) return "talk to agent";
            if (matchesAny(m, "aiuto", "ayuda", "aide", "hilfe", "ndihm", "help")) return "help";
            if (matchesAny(m, "ciao", "salut", "hallo", "hola", "bonjour", "buongiorno", "pershendetje")) return "hello";
            if (matchesAny(m, "grazie", "merci", "danke", "gracias", "faleminderit", "thanks")) return "thank you";
            if (matchesAny(m, "roaming", "itineranza", "itinerance")) return "roaming in italy";
        }
        return text;
    }

    private String detectFromKeywords(String m) {
        if (matchesAny(m, "pershendetje", "tungjatjeta", "kredi", "fatur", "mbush",
                "faleminderit", "ndihm", "miredita", "lek", "jam", "shqip",
                "paguaj", "borxh", "kariko", "oferta", "planin", "abonim", "rrjet", "miremengjes",
                "mirmengjes", "natemire", "naten", "tung", "cfare", "cilat", "sa",
                "ckemi", "mire", "flm", "falem"))
            return "sq";

        if (matchesAny(m, "buongiorno", "buonasera", "grazie", "ciao", "salve", "prego",
                "arrivederci", "per favore", "quanto", "conto", "fattura", "saldo",
                "ricarica", "internet", "rete", "wifi", "piano", "offerta", "agente",
                "operatore", "aiuto", "italiano"))
            return "it";
        if (matchesAny(m, "bonjour", "bonsoir", "merci", "salut", "svp", "s'il vous plait",
                "facture", "solde", "recharge", "reseau", "forfait", "offre",
                "agent", "operateur", "aide", "combien"))
            return "fr";
        if (matchesAny(m, "hallo", "guten tag", "guten morgen", "danke", "bitte",
                "rechnung", "guthaben", "aufladen", "netz", "tarif", "angebot",
                "mitarbeiter", "hilfe"))
            return "de";
        if (matchesAny(m, "hola", "buenos dias", "buenas tardes", "gracias", "por favor",
                "saldo", "factura", "recarga", "red", "wifi", "plan", "oferta",
                "agente", "ayuda"))
            return "es";

        return "en";
    }

    private boolean matchesAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }

    private String normalize(String text) {
        return text.toLowerCase().trim()
                .replace('\u00EB', 'e')
                .replace('\u00CB', 'E')
                .replace('\u00E7', 'c')
                .replace('\u00C7', 'C')
                .replace('\u00E9', 'e')
                .replace('\u00E8', 'e')
                .replace('\u00EA', 'e')
                .replace('\u00E0', 'a')
                .replace('\u00E2', 'a')
                .replace('\u00F4', 'o')
                .replace('\u00F9', 'u')
                .replace('\u00FB', 'u')
                .replace('\u00EE', 'i')
                .replace('\u00EF', 'i');
    }

    private String truncate(String text) {
        return text.length() > 50 ? text.substring(0, 47) + "..." : text;
    }
}
