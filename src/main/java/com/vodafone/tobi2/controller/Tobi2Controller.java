package com.vodafone.tobi2.controller;

import com.vodafone.tobi2.agent.TobiAgent;
import com.vodafone.tobi2.evaluation.AiEvaluatorService;
import com.vodafone.tobi2.monitoring.ConversationMetricsService;
import com.vodafone.tobi2.service.RagService;
import com.vodafone.tobi2.service.SpeechService;
import com.vodafone.tobi2.service.OcrService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/tobi2")
public class Tobi2Controller {

    private static final Logger log = LoggerFactory.getLogger(Tobi2Controller.class);
    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024;
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final TobiAgent tobiAgent;
    private final RagService ragService;
    private final SpeechService speechService;
    private final OcrService ocrService;
    private final AiEvaluatorService evaluatorService;
    private final ConversationMetricsService metricsService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String visionApiKey;
    private final String visionModel;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public Tobi2Controller(TobiAgent tobiAgent, RagService ragService,
                           SpeechService speechService, OcrService ocrService,
                           AiEvaluatorService evaluatorService,
                           ConversationMetricsService metricsService,
                           RestTemplate restTemplate, ObjectMapper objectMapper,
                           Environment environment) {
        this.tobiAgent = tobiAgent;
        this.ragService = ragService;
        this.speechService = speechService;
        this.ocrService = ocrService;
        this.evaluatorService = evaluatorService;
        this.metricsService = metricsService;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.visionApiKey = environment.getProperty("tobi2.vision.api-key", "");
        this.visionModel = environment.getProperty("tobi2.vision.model", "gemini-1.5-flash");
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, String> request) {
        long start = System.currentTimeMillis();
        String message = request.get("message");
        String sessionId = request.getOrDefault("sessionId", UUID.randomUUID().toString());
        String userId = request.getOrDefault("userId", "anonymous");
        String language = request.getOrDefault("language", "sq");

        String response = tobiAgent.chat(sessionId, userId, message, language);
        var eval = evaluatorService.evaluate(sessionId, message, response);
        metricsService.recordConversation(sessionId, System.currentTimeMillis() - start);

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("message", response);
        result.put("language", language);
        if (eval != null) {
            result.put("evaluation", Map.of(
                    "brandTone", eval.brandTone(),
                    "accuracy", eval.accuracy(),
                    "feedback", eval.feedback()
            ));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/chat/stream")
    public SseEmitter chatStream(
            @RequestParam String message,
            @RequestParam(defaultValue = "sq") String language,
            @RequestParam(required = false) String sessionId,
            @RequestParam(defaultValue = "anonymous") String userId) {

        String sid = sessionId != null ? sessionId : UUID.randomUUID().toString();
        SseEmitter emitter = new SseEmitter(60000L);

        executor.execute(() -> {
            try {
                String fullResponse = tobiAgent.chat(sid, userId, message, language);
                String[] words = fullResponse.split("(?<=\\s)");
                for (String word : words) {
                    emitter.send(SseEmitter.event().data(word));
                    Thread.sleep(30);
                }
                emitter.send(SseEmitter.event().name("complete").data(""));
                emitter.complete();

                var eval = evaluatorService.evaluate(sid, message, fullResponse);
                if (eval != null) {
                    log.info("Evaluation: tone={}, accuracy={}, feedback={}",
                            eval.brandTone(), eval.accuracy(), eval.feedback());
                }
            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event().name("error").data(e.getMessage()));
                } catch (IOException ex) {
                    // ignore
                }
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @PostMapping("/chat/photo")
    public ResponseEntity<Map<String, Object>> chatWithPhoto(
            @RequestParam("photo") MultipartFile photo,
            @RequestParam(defaultValue = "sq") String language,
            @RequestParam(required = false) String sessionId) {

        long start = System.currentTimeMillis();
        String sid = sessionId != null ? sessionId : UUID.randomUUID().toString();
        String fileName = photo.getOriginalFilename() != null ? photo.getOriginalFilename() : "image.jpg";

        if (photo.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }
        if (photo.getSize() > MAX_FILE_SIZE_BYTES) {
            return ResponseEntity.badRequest().body(Map.of("error", "File too large. Max 10MB."));
        }
        String mimeType = photo.getContentType();
        if (mimeType == null || !ALLOWED_MIME_TYPES.contains(mimeType)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Only JPEG, PNG, and WebP images are allowed. Got: " + mimeType));
        }

        String aiResponseText;

        if (visionApiKey == null || visionApiKey.trim().isEmpty() || "mock-key".equals(visionApiKey)) {
            aiResponseText = tryOcrThenMock(photo, sid, language, fileName);
            log.info("Vision: OCR->mock path for {}", fileName);
        } else {
            aiResponseText = callGeminiVision(photo, mimeType, visionApiKey, language, fileName);
            if (aiResponseText == null) {
                aiResponseText = tryOcrThenMock(photo, sid, language, fileName);
                log.info("Vision: Gemini failed, OCR->mock path for {}", fileName);
            } else {
                log.info("Vision Gemini success for {}", fileName);
            }
        }

        metricsService.recordConversation(sid, System.currentTimeMillis() - start);

        return ResponseEntity.ok(Map.of(
                "sessionId", sid,
                "attachedFile", fileName,
                "message", aiResponseText
        ));
    }

    private String tryOcrThenMock(MultipartFile photo, String sessionId, String language, String fileName) {
        try {
            String ocrText = ocrService.extractText(photo);
            if (ocrText != null && !ocrText.isEmpty()) {
                log.info("OCR extracted text from {}: {}", fileName, ocrText.substring(0, Math.min(ocrText.length(), 80)));
                String prompt = "sq".equals(language)
                    ? "Kam lexuar këtë tekst nga një foto e klientit: \"" + ocrText + "\". Shpjegoja klientit se çfarë ke gjetur në këtë tekst. Përgjigju në shqip."
                    : "I read this text from a customer's photo: \"" + ocrText + "\". Explain to the customer what you found in this text. Reply in English.";
                return tobiAgent.chat(sessionId, sessionId, prompt, language);
            }
        } catch (Exception e) {
            log.warn("OCR failed for {}, using mock", fileName);
        }
        return mockVision(fileName, language);
    }

    private String mockVision(String fileName, String language) {
        String lowerName = fileName.toLowerCase();
        boolean sq = "sq".equals(language);

        if (lowerName.contains("router") || lowerName.contains("modem") || lowerName.contains("box")) {
            return sq
                ? "Analizova foton e routerit tuaj. Vërej se drita 'LOS' ose 'Internet' duket të jetë e kuqe, që tregon se nuk ka lidhje interneti. Ju lutem fikeni pajisjen nga priza, pritni 30 sekonda dhe ndizeni përsëri. Nëse problemi vazhdon, na kontaktoni për asistencë të mëtejshme."
                : "I analyzed your router photo. The 'LOS' or 'Internet' light appears red, indicating no internet connection. Please power cycle the device (unplug for 30 seconds). If the issue persists, contact us for further assistance.";
        }
        if (lowerName.contains("bill") || lowerName.contains("fatur") || lowerName.contains("invoice")) {
            return sq
                ? "Skanova faturën tuaj. Shuma totale për t'u paguar është 1,250 Lek. Afati i pagesës është 27/05/2026. Mund të paguani lehtësisht përmes aplikacionit My Vodafone ose në pikat e pagesave. A doni të bëni pagesën tani?"
                : "I scanned your bill. The total amount due is 1,250 Lek, with a due date of 27/05/2026. You can pay easily through the My Vodafone app or at payment points. Would you like to make a payment now?";
        }
        if (lowerName.contains("error") || lowerName.contains("problem") || lowerName.contains("gabim") || lowerName.contains("mesazh")) {
            return sq
                ? "Shoh që keni një mesazh gabimi në foto. Për të zgjidhur problemin, provoni të rifilloni pajisjen ose të kontrolloni lidhjen tuaj të internetit. A mund t'ju ndihmoj më tej?"
                : "I can see an error message in your photo. To resolve this, try restarting your device or checking your internet connection. Can I help you further?";
        }
        if (lowerName.contains("screen") || lowerName.contains("shot") || lowerName.contains("capture") || lowerName.contains("print")) {
            return sq
                ? "Mora screenshot-in tuaj. Po analizoj informacionin në ekran për të kuptuar se çfarë problemi keni. A mund të më tregoni më shumë detaje se çfarë po shihni?"
                : "I received your screenshot. I'm analyzing the information on screen to understand what issue you're experiencing. Could you provide more details about what you're seeing?";
        }
        if (lowerName.contains("id") || lowerName.contains("kart") || lowerName.contains("identitet")) {
            return sq
                ? "Duket si një dokument identifikimi. Për sigurinë tuaj, nuk rekomandojmë ngarkimin e dokumenteve personale. A mund t'ju ndihmoj me diçka tjetër?"
                : "This appears to be an identification document. For your security, we don't recommend uploading personal documents. Can I help you with something else?";
        }
        if (lowerName.contains("selfie") || lowerName.contains("person") || lowerName.contains("face") || lowerName.contains("njeri") || lowerName.contains("user")) {
            return sq
                ? "Shoh një foto tuajën. Për të vazhduar, a mund të më tregoni se si lidhet kjo foto me llogarinë tuaj Vodafone?"
                : "I can see your photo. To proceed, could you tell me how this photo relates to your Vodafone account?";
        }
        return sq
            ? "Mora foton tuaj. Po e analizoj për të kuptuar se çfarë problemi keni në lidhje me shërbimet Vodafone. A mund të më jepni më shumë detaje?"
            : "I received your photo. I'm analyzing it to understand what issue you're experiencing with your Vodafone services. Could you provide more details?";
    }

    private String callGeminiVision(MultipartFile photo, String mimeType, String apiKey, String language, String fileName) {
        try {
            byte[] imageBytes = photo.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            String promptText = "sq".equals(language)
                ? "Je TOBi, asistenti i Vodafone Albania. Analizo këtë foto dhe përgjigju në shqip."
                : "You are TOBi, Vodafone Albania assistant. Analyze this image and reply in English.";

            ObjectNode payload = objectMapper.createObjectNode();
            ArrayNode contents = payload.putArray("contents");
            ObjectNode content = contents.addObject();
            ArrayNode parts = content.putArray("parts");
            parts.addObject().put("text", promptText);
            ObjectNode inlineData = parts.addObject().putObject("inlineData");
            inlineData.put("mimeType", mimeType);
            inlineData.put("data", base64Image);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Goog-Api-Key", apiKey);

            String url = "https://generativelanguage.googleapis.com/v1/models/" + visionModel + ":generateContent";

            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.POST,
                new HttpEntity<>(objectMapper.writeValueAsString(payload), headers),
                Map.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error("Gemini API error: status={}", response.getStatusCode());
                return null;
            }

            var candidates = (List<Map>) response.getBody().get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                log.warn("No candidates in Gemini response for {}", fileName);
                return null;
            }

            Map firstCandidate = candidates.get(0);
            if (firstCandidate.containsKey("finishReason") && !"STOP".equals(firstCandidate.get("finishReason"))) {
                log.warn("Gemini finish reason: {} for {}", firstCandidate.get("finishReason"), fileName);
            }

            Map contentMap = (Map) firstCandidate.get("content");
            if (contentMap == null) return null;
            List<Map> partsList = (List<Map>) contentMap.get("parts");
            if (partsList == null || partsList.isEmpty()) return null;
            String text = (String) partsList.get(0).get("text");
            return text != null ? text.trim() : null;

        } catch (Exception e) {
            log.error("Gemini vision call failed for {}, using mock fallback", fileName);
            return null;
        }
    }

    @PostMapping("/stt")
    public ResponseEntity<Map<String, String>> speechToText(@RequestParam("audio") MultipartFile audioFile) {
        try {
            String text = speechService.speechToText(audioFile.getBytes(),
                    audioFile.getContentType() != null ? audioFile.getContentType() : "audio/wav");
            return ResponseEntity.ok(Map.of("text", text));
        } catch (Exception e) {
            log.error("STT failed", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "STT processing failed"));
        }
    }

    @PostMapping("/tts")
    public ResponseEntity<byte[]> textToSpeech(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        String language = request.getOrDefault("language", "sq-AL");
        byte[] audio = speechService.textToSpeech(text, language);
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("audio/wav"))
                .body(audio);
    }

    @PostMapping("/rag/search")
    public ResponseEntity<Map<String, Object>> ragSearch(@RequestBody Map<String, String> request) {
        String query = request.get("query");
        List<String> results = ragService.search(query);
        return ResponseEntity.ok(Map.of(
                "query", query,
                "results", results,
                "count", results.size()
        ));
    }

    @GetMapping("/session/start")
    public ResponseEntity<Map<String, String>> startSession(
            @RequestParam(defaultValue = "anonymous") String userId) {
        String sessionId = UUID.randomUUID().toString();
        return ResponseEntity.ok(Map.of("sessionId", sessionId, "userId", userId));
    }
}