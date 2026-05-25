package com.vodafone.tobi2.tests;

import com.vodafone.tobi2.service.SpeechService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SpeechService Tests")
class SpeechServiceTest {

    private SpeechService speechService;

    @BeforeEach
    void setUp() {
        speechService = new SpeechService();
        ReflectionTestUtils.setField(speechService, "sttLanguage", "sq-AL");
        ReflectionTestUtils.setField(speechService, "ttsLanguage", "sq-AL");
        ReflectionTestUtils.setField(speechService, "provider", "mock");
    }

    @Test
    @DisplayName("testSpeechToText: mock audio bytes -> returns Albanian string")
    void testSpeechToText() {
        byte[] mockAudio = new byte[100];
        String result = speechService.speechToText(mockAudio, "audio/wav");

        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result).contains("Përshëndetje");
        assertThat(result).contains("kredi");
    }

    @Test
    @DisplayName("testTextToSpeech: text input -> returns UTF-8 bytes")
    void testTextToSpeech() {
        String inputText = "Pershendetje, kjo eshte nje test.";
        byte[] result = speechService.textToSpeech(inputText, "sq-AL");

        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();

        String decoded = new String(result, StandardCharsets.UTF_8);
        assertThat(decoded).isEqualTo(inputText);
    }
}
