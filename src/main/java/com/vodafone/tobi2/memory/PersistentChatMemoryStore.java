package com.vodafone.tobi2.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PersistentChatMemoryStore {

    private static final Logger log = LoggerFactory.getLogger(PersistentChatMemoryStore.class);

    private final JdbcTemplate jdbc;

    public PersistentChatMemoryStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        jdbc.execute("CREATE TABLE IF NOT EXISTS tobi2_memory ("
                + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                + "session_id VARCHAR(255), "
                + "role VARCHAR(50), "
                + "content TEXT, "
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ")");
    }

    public void addMessage(String sessionId, String role, String content) {
        jdbc.update("INSERT INTO tobi2_memory (session_id, role, content) VALUES (?, ?, ?)",
                sessionId, role, content);
    }

    public List<String> getHistory(String sessionId) {
        return jdbc.query("SELECT role, content FROM tobi2_memory WHERE session_id = ? ORDER BY created_at ASC",
                (rs, rowNum) -> rs.getString("role") + ": " + rs.getString("content"),
                sessionId);
    }

    public void deleteSession(String sessionId) {
        jdbc.update("DELETE FROM tobi2_memory WHERE session_id = ?", sessionId);
    }
}
