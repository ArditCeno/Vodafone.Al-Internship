package com.vodafone.tobi2.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class OcrService {

    private static final Logger log = LoggerFactory.getLogger(OcrService.class);

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String apiUrl;

    public OcrService(RestTemplate restTemplate,
                      @Value("${tobi2.ocr.api-key:helloworld}") String apiKey,
                      @Value("${tobi2.ocr.api-url:https://api.ocr.space/parse/image}") String apiUrl) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
    }

    public String extractText(MultipartFile photo) {
        try {
            byte[] imageBytes = photo.getBytes();
            String base64Image = java.util.Base64.getEncoder().encodeToString(imageBytes);
            String mimeType = photo.getContentType() != null ? photo.getContentType() : "image/jpeg";

            String body = "apikey=" + apiKey
                    + "&base64Image=" + java.net.URLEncoder.encode("data:" + mimeType + ";base64," + base64Image, "UTF-8")
                    + "&language=eng&sqi"
                    + "&OCREngine=2"
                    + "&isTable=false"
                    + "&scale=true";

            var headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

            var request = new org.springframework.http.HttpEntity<>(body, headers);
            var response = restTemplate.postForEntity(apiUrl, request, Map.class);

            if (response.getBody() == null) return null;

            int exitCode = ((Number) response.getBody().getOrDefault("OCRExitCode", 99)).intValue();
            if (exitCode != 1) {
                log.warn("OCR failed with exit code {}: {}", exitCode, response.getBody().get("ErrorMessage"));
                return null;
            }

            var parsedResults = (java.util.List<Map>) response.getBody().get("ParsedResults");
            if (parsedResults == null || parsedResults.isEmpty()) return null;

            String text = (String) parsedResults.get(0).get("ParsedText");
            return (text != null && !text.trim().isEmpty()) ? text.trim() : null;

        } catch (Exception e) {
            log.error("OCR extraction failed", e);
            return null;
        }
    }
}
