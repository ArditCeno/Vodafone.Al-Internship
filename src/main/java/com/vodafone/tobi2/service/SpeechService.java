package com.vodafone.tobi2.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class SpeechService {

    private static final Logger log = LoggerFactory.getLogger(SpeechService.class);

    @Value("${tobi2.speech.stt-language:sq-AL}")
    private String sttLanguage;

    @Value("${tobi2.speech.tts-language:sq-AL}")
    private String ttsLanguage;

    @Value("${tobi2.speech.provider:mock}")
    private String provider;

    public String speechToText(byte[] audioData, String format) {
        log.info("STT: {} bytes, format={}, language={}", audioData.length, format, sttLanguage);
        return "Përshëndetje, dua të di sa kredi kam.";
    }

    public byte[] textToSpeech(String text, String language) {
        log.info("TTS: text='{}', language={}", text, language != null ? language : ttsLanguage);
        return text.getBytes(StandardCharsets.UTF_8);
    }
}
