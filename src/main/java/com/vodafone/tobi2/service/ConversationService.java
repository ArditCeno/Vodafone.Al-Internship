package com.vodafone.tobi2.service;

import com.vodafone.tobi2.model.ChatMessage;
import com.vodafone.tobi2.model.Conversation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ConversationService {

    private static final Logger log = LoggerFactory.getLogger(ConversationService.class);

    private final JdbcTemplate jdbc;

    private final ConversationRowMapper conversationMapper = new ConversationRowMapper();
    private final ChatMessageRowMapper messageMapper = new ChatMessageRowMapper();

    public ConversationService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public String createSession(String userId) {
        String sessionId = UUID.randomUUID().toString();
        jdbc.update(
            "INSERT INTO tobi2_conversations (user_id, session_id) VALUES (?, ?)",
            userId, sessionId);
        log.debug("Created new session {} for user {}", sessionId, userId);
        return sessionId;
    }

    public void createSession(String userId, String sessionId) {
        jdbc.update(
            "INSERT INTO tobi2_conversations (user_id, session_id) VALUES (?, ?)",
            userId, sessionId);
        log.debug("Created session {} for user {} (existing ID)", sessionId, userId);
    }

    public void updateSessionTitle(String sessionId, String title) {
        jdbc.update(
            "UPDATE tobi2_conversations SET title = ? WHERE session_id = ?",
            title.length() > 200 ? title.substring(0, 200) : title, sessionId);
    }

    public void endSession(String sessionId) {
        jdbc.update(
            "UPDATE tobi2_conversations SET is_active = FALSE WHERE session_id = ?",
            sessionId);
    }

    public List<Conversation> getConversations(String userId) {
        return jdbc.query(
            "SELECT * FROM tobi2_conversations WHERE user_id = ? ORDER BY started_at DESC",
            conversationMapper, userId);
    }

    public Optional<Conversation> getConversation(String sessionId) {
        var list = jdbc.query(
            "SELECT * FROM tobi2_conversations WHERE session_id = ?",
            conversationMapper, sessionId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public void addMessage(String userId, String sessionId, String role, String content) {
        jdbc.update(
            "INSERT INTO tobi2_messages (user_id, session_id, role, content) VALUES (?, ?, ?, ?)",
            userId, sessionId, role, content);
    }

    public List<ChatMessage> getMessages(String sessionId) {
        return jdbc.query(
            "SELECT * FROM tobi2_messages WHERE session_id = ? ORDER BY created_at ASC",
            messageMapper, sessionId);
    }

    public boolean sessionBelongsToUser(String sessionId, String userId) {
        var conv = getConversation(sessionId);
        return conv.isPresent() && conv.get().userId().equals(userId);
    }

    private static class ConversationRowMapper implements RowMapper<Conversation> {
        @Override
        public Conversation mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Conversation(
                rs.getLong("id"),
                rs.getString("user_id"),
                rs.getString("session_id"),
                rs.getString("title"),
                rs.getString("started_at"),
                rs.getBoolean("is_active")
            );
        }
    }

    private static class ChatMessageRowMapper implements RowMapper<ChatMessage> {
        @Override
        public ChatMessage mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ChatMessage(
                rs.getLong("id"),
                rs.getString("user_id"),
                rs.getString("session_id"),
                rs.getString("role"),
                rs.getString("content"),
                rs.getString("created_at")
            );
        }
    }
}
