package com.vodafone.tobi2.tests;

import com.vodafone.tobi2.memory.PersistentChatMemoryStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PersistentChatMemoryStore Tests")
@SpringBootTest
@ActiveProfiles("test")
class PersistentChatMemoryStoreTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private PersistentChatMemoryStore memoryStore;
    private String sessionId1;
    private String sessionId2;

    @BeforeEach
    void setUp() {
        try {
            jdbcTemplate.execute("DROP TABLE IF EXISTS tobi2_memory");
        } catch (Exception e) {
        }
        memoryStore = new PersistentChatMemoryStore(jdbcTemplate);
        sessionId1 = UUID.randomUUID().toString();
        sessionId2 = UUID.randomUUID().toString();
    }

    @Test
    @DisplayName("testAddAndGetHistory: add 3 messages -> retrieve in order")
    void testAddAndGetHistory() {
        memoryStore.addMessage(sessionId1, "user", "Hello");
        memoryStore.addMessage(sessionId1, "assistant", "Hi there");
        memoryStore.addMessage(sessionId1, "user", "How are you?");

        List<String> history = memoryStore.getHistory(sessionId1);

        assertThat(history).hasSize(3);
        assertThat(history.get(0)).isEqualTo("user: Hello");
        assertThat(history.get(1)).isEqualTo("assistant: Hi there");
        assertThat(history.get(2)).isEqualTo("user: How are you?");
    }

    @Test
    @DisplayName("testDeleteSession: add messages -> delete -> empty history")
    void testDeleteSession() {
        memoryStore.addMessage(sessionId1, "user", "Message 1");
        memoryStore.addMessage(sessionId1, "assistant", "Response 1");

        List<String> historyBefore = memoryStore.getHistory(sessionId1);
        assertThat(historyBefore).hasSize(2);

        memoryStore.deleteSession(sessionId1);

        List<String> historyAfter = memoryStore.getHistory(sessionId1);
        assertThat(historyAfter).isEmpty();
    }

    @Test
    @DisplayName("testMultipleSessions: two sessions -> histories are isolated")
    void testMultipleSessions() {
        memoryStore.addMessage(sessionId1, "user", "Session 1 Message");
        memoryStore.addMessage(sessionId1, "assistant", "Session 1 Response");

        memoryStore.addMessage(sessionId2, "user", "Session 2 Message A");
        memoryStore.addMessage(sessionId2, "assistant", "Session 2 Response A");
        memoryStore.addMessage(sessionId2, "user", "Session 2 Message B");

        List<String> history1 = memoryStore.getHistory(sessionId1);
        List<String> history2 = memoryStore.getHistory(sessionId2);

        assertThat(history1).hasSize(2);
        assertThat(history2).hasSize(3);

        assertThat(history1.get(0)).contains("Session 1");
        assertThat(history2.get(0)).contains("Session 2");
    }
}
