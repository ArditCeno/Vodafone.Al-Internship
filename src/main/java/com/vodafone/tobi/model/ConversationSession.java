package com.vodafone.tobi.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ConversationSession implements Serializable {

    private String sessionId;
    private String userId;
    private String watsonxSessionId;
    private String channel;
    private String language;
    private LocalDateTime createdAt;
    private LocalDateTime lastActivity;
    private int messageCount;
    private Map<String, Object> context;
    private boolean escalatedToAgent;
    private String agentId;

    public ConversationSession() {
        this.createdAt = LocalDateTime.now();
        this.lastActivity = LocalDateTime.now();
        this.context = new HashMap<>();
        this.messageCount = 0;
        this.escalatedToAgent = false;
    }

    public ConversationSession(String sessionId, String userId, String channel, String language) {
        this();
        this.sessionId = sessionId;
        this.userId = userId;
        this.channel = channel;
        this.language = language;
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getWatsonxSessionId() { return watsonxSessionId; }
    public void setWatsonxSessionId(String watsonxSessionId) { this.watsonxSessionId = watsonxSessionId; }
    
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getLastActivity() { return lastActivity; }
    public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }
    
    public int getMessageCount() { return messageCount; }
    public void setMessageCount(int messageCount) { this.messageCount = messageCount; }
    
    public Map<String, Object> getContext() { return context; }
    public void setContext(Map<String, Object> context) { this.context = context; }
    
    public boolean isEscalatedToAgent() { return escalatedToAgent; }
    public void setEscalatedToAgent(boolean escalatedToAgent) { this.escalatedToAgent = escalatedToAgent; }
    
    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
    
    public void incrementMessageCount() { this.messageCount++; }
    public void updateLastActivity() { this.lastActivity = LocalDateTime.now(); }
}
