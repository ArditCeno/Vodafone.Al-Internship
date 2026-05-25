package com.vodafone.tobi2.tests;

import com.vodafone.tobi2.agent.TobiAgentImpl;
import com.vodafone.tobi2.memory.PersistentChatMemoryStore;
import com.vodafone.tobi2.recommender.PlanRecommenderService;
import com.vodafone.tobi2.service.translation.MockTranslationService;
import com.vodafone.tobi2.service.translation.TranslationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("TobiAgentImpl Tests")
@ExtendWith(MockitoExtension.class)
class TobiAgentImplTest {

    @Mock
    private PersistentChatMemoryStore memoryStore;

    @Mock
    private PlanRecommenderService planRecommender;

    private TobiAgentImpl tobiAgent;
    private TranslationService translationService;
    private String sessionId;

    @BeforeEach
    void setUp() {
        translationService = new MockTranslationService();
        tobiAgent = new TobiAgentImpl(memoryStore, translationService, planRecommender, "mock");
        sessionId = UUID.randomUUID().toString();
    }

    @Test
    @DisplayName("testFirstMessage_showsLanguageSelector: first message -> language prompt")
    void testFirstMessage_showsLanguageSelector() {
        when(memoryStore.getHistory(anyString())).thenReturn(new ArrayList<>());

        String response = tobiAgent.chat(sessionId, "user1", "sa kredi kam", "sq");

        assertThat(response).contains("Zgjidhni gjuhën");
        assertThat(response).contains("Shtypni 1");
        assertThat(response).contains("Press 2");
    }

    @Test
    @DisplayName("testSelectAlbanian: send '1' after pending -> processes in Albanian")
    void testSelectAlbanian() {
        when(memoryStore.getHistory(anyString())).thenReturn(new ArrayList<>());

        String firstResponse = tobiAgent.chat(sessionId, "user1", "sa kredi kam", "sq");
        assertThat(firstResponse).contains("Zgjidhni gjuhën");

        List<String> historyAfterFirst = List.of("assistant: " + firstResponse);
        when(memoryStore.getHistory(anyString())).thenReturn(historyAfterFirst);

        String secondResponse = tobiAgent.chat(sessionId, "user1", "1", "sq");

        assertThat(secondResponse).contains("Shqip u zgjodh");
        assertThat(secondResponse).contains("2,450 Lek");
    }

    @Test
    @DisplayName("testSelectEnglish: send '2' after pending -> processes in English")
    void testSelectEnglish() {
        when(memoryStore.getHistory(anyString())).thenReturn(new ArrayList<>());

        String firstResponse = tobiAgent.chat(sessionId, "user1", "what is my balance", "en");
        assertThat(firstResponse).contains("Zgjidhni gjuhën");

        List<String> historyAfterFirst = List.of("assistant: " + firstResponse);
        when(memoryStore.getHistory(anyString())).thenReturn(historyAfterFirst);

        String secondResponse = tobiAgent.chat(sessionId, "user1", "2", "en");

        assertThat(secondResponse).contains("English selected");
        assertThat(secondResponse).contains("2,450 Lek");
        assertThat(secondResponse).contains("Your current balance");
    }

    @Test
    @DisplayName("testDetectLanguage_Albanian: keywords -> sq")
    void testDetectLanguage_Albanian() {
        String result = detectLanguageViaReflection("pershendetje dua kredi");
        assertThat(result).isEqualTo("sq");
    }

    @Test
    @DisplayName("testDetectLanguage_English: no Albanian keywords -> en")
    void testDetectLanguage_English() {
        String result = detectLanguageViaReflection("hello what is my balance");
        assertThat(result).isEqualTo("en");
    }

    private String detectLanguageViaReflection(String message) {
        return (String) ReflectionTestUtils.invokeMethod(tobiAgent, "detectLanguage", message);
    }
}
