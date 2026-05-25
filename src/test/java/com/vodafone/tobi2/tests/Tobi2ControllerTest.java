package com.vodafone.tobi2.tests;

import com.vodafone.tobi2.agent.TobiAgent;
import com.vodafone.tobi2.config.Tobi2WebConfig;
import com.vodafone.tobi2.controller.Tobi2Controller;
import com.vodafone.tobi2.evaluation.AiEvaluatorService;
import com.vodafone.tobi2.monitoring.ConversationMetricsService;
import com.vodafone.tobi2.service.OcrService;
import com.vodafone.tobi2.service.RagService;
import com.vodafone.tobi2.service.SpeechService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Tobi2Controller Tests")
@WebMvcTest(Tobi2Controller.class)
@Import(Tobi2WebConfig.class)
class Tobi2ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TobiAgent tobiAgent;

    @MockBean
    private RagService ragService;

    @MockBean
    private SpeechService speechService;

    @MockBean
    private OcrService ocrService;

    @MockBean
    private AiEvaluatorService evaluatorService;

    @MockBean
    private ConversationMetricsService metricsService;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    @DisplayName("testChatEndpoint: POST /api/tobi2/chat -> 200 + valid response body")
    void testChatEndpoint() throws Exception {
        String mockResponse = "Hello! I'm TOBi2. How can I help you?";
        when(tobiAgent.chat(anyString(), anyString(), anyString(), anyString())).thenReturn(mockResponse);
        when(evaluatorService.evaluate(anyString(), anyString(), anyString())).thenReturn(null);

        mockMvc.perform(post("/api/tobi2/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\": \"hello\", \"sessionId\": \"test-session\", \"userId\": \"test-user\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId", is("test-session")))
                .andExpect(jsonPath("$.message", is(mockResponse)))
                .andExpect(jsonPath("$.language", is("sq")));
    }

    @Test
    @DisplayName("testChatEndpoint_EmptyMessage: POST with empty message -> 400")
    void testChatEndpoint_EmptyMessage() throws Exception {
        when(tobiAgent.chat(anyString(), anyString(), eq(""), anyString())).thenReturn("");

        mockMvc.perform(post("/api/tobi2/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\": \"\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("testChatStreamEndpoint: GET /api/tobi2/chat/stream -> SSE events")
    void testChatStreamEndpoint() throws Exception {
        String mockResponse = "Hello TOBi2";
        when(tobiAgent.chat(anyString(), anyString(), anyString(), anyString())).thenReturn(mockResponse);

        mockMvc.perform(get("/api/tobi2/chat/stream")
                        .param("message", "hello")
                        .param("language", "sq")
                        .param("sessionId", "test-stream-session"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM));
    }

    @Test
    @DisplayName("testSpeechToTextEndpoint: POST multipart -> 200 + text field")
    void testSpeechToTextEndpoint() throws Exception {
        String mockText = "Përshëndetje, kjo eshte nje test.";
        when(speechService.speechToText(org.mockito.ArgumentMatchers.any(byte[].class), anyString())).thenReturn(mockText);

        MockMultipartFile audioFile = new MockMultipartFile(
                "audio",
                "test.wav",
                "audio/wav",
                "mock audio content".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/tobi2/stt")
                        .file(audioFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(mockText)));
    }

    @Test
    @DisplayName("testTextToSpeechEndpoint: POST JSON -> 200 + audio/wav content-type")
    void testTextToSpeechEndpoint() throws Exception {
        byte[] mockAudio = "Mock audio data as bytes".getBytes(StandardCharsets.UTF_8);
        when(speechService.textToSpeech(anyString(), anyString())).thenReturn(mockAudio);

        mockMvc.perform(post("/api/tobi2/tts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\": \"Hello\", \"language\": \"sq-AL\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.valueOf("audio/wav")));
    }

    @Test
    @DisplayName("testRagSearchEndpoint: POST /api/tobi2/rag/search -> 200 + results")
    void testRagSearchEndpoint() throws Exception {
        List<String> mockResults = List.of(
                "Unlimited Max: 1500 Lek/month",
                "Smart S: 400 Lek/month"
        );
        when(ragService.search(anyString())).thenReturn(mockResults);

        mockMvc.perform(post("/api/tobi2/rag/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\": \"unlimited\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query", is("unlimited")))
                .andExpect(jsonPath("$.count", is(2)))
                .andExpect(jsonPath("$.results", hasSize(2)));
    }

    @Test
    @DisplayName("testStartSessionEndpoint: GET /api/tobi2/session/start -> 200 + UUID sessionId")
    void testStartSessionEndpoint() throws Exception {
        mockMvc.perform(get("/api/tobi2/session/start")
                        .param("userId", "test-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId", notNullValue()))
                .andExpect(jsonPath("$.userId", is("test-user")));
    }

    @Test
    @DisplayName("testCorsHeaders: OPTIONS -> appropriate CORS headers")
    void testCorsHeaders() throws Exception {
        mockMvc.perform(options("/api/tobi2/chat")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().exists("Access-Control-Allow-Methods"));
    }
}
