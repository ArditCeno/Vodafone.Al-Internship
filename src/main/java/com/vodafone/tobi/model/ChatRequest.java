package com.vodafone.tobi.model;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public class ChatRequest {

    @NotBlank(message = "Message cannot be empty")
    private String message;
    
    private String sessionId;
    private String userId;
    private String channel;
    private String language;
    private Map<String, Object> context;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public Map<String, Object> getContext() { return context; }
    public void setContext(Map<String, Object> context) { this.context = context; }
}
