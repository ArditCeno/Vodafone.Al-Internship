package com.vodafone.tobi2.tests;

import com.vodafone.tobi2.service.MockChatModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MockChatModel Tests")
class MockChatModelTest {

    private MockChatModel chatModel;

    @BeforeEach
    void setUp() {
        chatModel = new MockChatModel();
    }

    @Test
    @DisplayName("testGreeting_Albanian: input 'pershendetje' -> contains welcome intro")
    void testGreeting_Albanian() {
        String response = chatModel.generate("pershendetje", "sq", List.of());
        assertThat(response).contains("TOBi2.0");
        assertThat(response).contains("Përshëndetje");
        assertThat(response).contains("asistenti juaj inteligjent");
    }

    @Test
    @DisplayName("testGreeting_English: input 'hello' -> contains welcome in English")
    void testGreeting_English() {
        String response = chatModel.generate("hello", "en", List.of());
        assertThat(response).contains("TOBi2.0");
        assertThat(response).contains("Hello");
        assertThat(response).contains("your intelligent Vodafone Albania assistant");
    }

    @Test
    @DisplayName("testBalanceQuery: input 'sa kredi kam' -> contains '2,450 Lek'")
    void testBalanceQuery() {
        String response = chatModel.generate("sa kredi kam", "sq", List.of());
        assertThat(response).contains("2,450 Lek");
        assertThat(response).contains("Kredia juaj aktuale");
    }

    @Test
    @DisplayName("testBillQuery: input 'fatura ime' -> contains '1,250 Lek'")
    void testBillQuery() {
        String response = chatModel.generate("fatura ime", "sq", List.of());
        assertThat(response).contains("1,250 Lek");
        assertThat(response).contains("Fatura juaj");
    }

    @Test
    @DisplayName("testTopUpQuery: input 'si te mbush kredit' -> contains top-up instructions")
    void testTopUpQuery() {
        String response = chatModel.generate("si te mbush kredit", "sq", List.of());
        assertThat(response).contains("mbushur kredite");
        assertThat(response).contains("voucher");
    }

    @Test
    @DisplayName("testInternetIssue: input 'internet nuk punon' -> contains data amount")
    void testInternetIssue() {
        String response = chatModel.generate("internet nuk punon", "sq", List.of());
        assertThat(response).contains("GB");
        assertThat(response).contains("Perdorimi i internetit");
    }

    @Test
    @DisplayName("testPlansInquiry: input 'planet tua' -> contains plan names")
    void testPlansInquiry() {
        String response = chatModel.generate("planet tua", "sq", List.of());
        assertThat(response).contains("Unlimited Max");
        assertThat(response).contains("Unlimited L");
        assertThat(response).contains("Unlimited M");
        assertThat(response).contains("Smart S");
    }

    @Test
    @DisplayName("testRoamingQuery: input 'roaming ne itali' -> contains cost info")
    void testRoamingQuery() {
        String response = chatModel.generate("roaming ne itali", "sq", List.of());
        assertThat(response).contains("Itali");
        assertThat(response).contains("EUR");
        assertThat(response).contains("0.05");
    }

    @Test
    @DisplayName("testNetworkQuery: input 'nderprerje' -> network status response")
    void testNetworkQuery() {
        String response = chatModel.generate("nderprerje", "sq", List.of());
        assertThat(response).contains("ne rregull");
    }

    @Test
    @DisplayName("test5GQuery: input '5g' -> contains coverage info")
    void test5GQuery() {
        String response = chatModel.generate("5g", "sq", List.of());
        assertThat(response).contains("Tirane");
        assertThat(response).contains("Vodafone Albania");
    }

    @Test
    @DisplayName("testAgentHandoff: input 'flas me nje agjent' -> contains handoff message")
    void testAgentHandoff() {
        String response = chatModel.generate("flas me nje agjent", "sq", List.of());
        assertThat(response).contains("agjent");
        assertThat(response).contains("lidhim");
    }

    @Test
    @DisplayName("testHelpCommand: input 'ndihme' -> shows available commands")
    void testHelpCommand() {
        String response = chatModel.generate("ndihmë", "sq", List.of());
        assertThat(response).contains("Sa kredi kam");
        assertThat(response).contains("fatura");
        assertThat(response).contains("internet");
    }

    @Test
    @DisplayName("testFallback: input 'xyz123abc' -> \"didn't quite understand\"")
    void testFallback() {
        String response = chatModel.generate("xyz123abc", "en", List.of());
        assertThat(response).contains("didn't quite understand");
    }

    @Test
    @DisplayName("testEnglishResponseForEnglishInput: input 'what is my balance' -> English response")
    void testEnglishResponseForEnglishInput() {
        String response = chatModel.generate("what is my balance", "en", List.of());
        assertThat(response).contains("Your current balance");
        assertThat(response).contains("2,450 Lek");
    }
}
