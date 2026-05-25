package com.vodafone.tobi2.service.translation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@ConditionalOnProperty(name = "tobi2.translation.provider", havingValue = "google")
public class GoogleTranslationService implements TranslationService {

    private static final Logger log = LoggerFactory.getLogger(GoogleTranslationService.class);
    private static final String API_URL = "https://translate.googleapis.com/translate_a/single";

    private final RestTemplate restTemplate;

    public GoogleTranslationService() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String detectLanguage(String text) {
        try {
            String url = API_URL + "?client=gtx&sl=auto&tl=en&dt=t&q=" + encode(text);
            String json = restTemplate.getForObject(url, String.class);
            if (json != null) {
                @SuppressWarnings("unchecked")
                List<Object> root = parseJsonArray(json);
                if (root.size() > 2 && root.get(2) instanceof String lang) {
                    log.debug("Google: detected '{}' for: {}", lang, truncate(text));
                    return lang;
                }
            }
        } catch (Exception e) {
            log.warn("Google detect failed: {}", e.getMessage());
        }
        return "en";
    }

    @Override
    public String translate(String text, String targetLanguage) {
        String detected = detectLanguage(text);
        return translate(text, detected, targetLanguage);
    }

    @Override
    public String translate(String text, String sourceLanguage, String targetLanguage) {
        if (sourceLanguage.equals(targetLanguage)) return text;
        try {
            String url = API_URL + "?client=gtx&sl=" + sourceLanguage + "&tl=" + targetLanguage + "&dt=t&q=" + encode(text);
            String json = restTemplate.getForObject(url, String.class);
            if (json != null) {
                @SuppressWarnings("unchecked")
                List<Object> root = parseJsonArray(json);
                @SuppressWarnings("unchecked")
                List<Object> sentences = (List<Object>) root.get(0);
                StringBuilder result = new StringBuilder();
                for (Object s : sentences) {
                    @SuppressWarnings("unchecked")
                    List<Object> parts = (List<Object>) s;
                    if (parts.get(0) != null) {
                        result.append(parts.get(0));
                    }
                }
                log.debug("Google: {} -> {}: {}", sourceLanguage, targetLanguage, truncate(result.toString()));
                return result.toString();
            }
        } catch (Exception e) {
            log.warn("Google translate failed: {}", e.getMessage());
        }
        return text;
    }

    @SuppressWarnings("unchecked")
    private List<Object> parseJsonArray(String json) {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        try {
            return mapper.readValue(json, List.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String encode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }

    private String truncate(String text) {
        return text.length() > 50 ? text.substring(0, 47) + "..." : text;
    }
}
