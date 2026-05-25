package com.vodafone.tobi2.service.translation;

public interface TranslationService {
    String detectLanguage(String text);
    String translate(String text, String targetLanguage);
    String translate(String text, String sourceLanguage, String targetLanguage);
}
