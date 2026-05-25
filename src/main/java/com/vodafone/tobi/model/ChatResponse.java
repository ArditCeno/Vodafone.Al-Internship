package com.vodafone.tobi.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ChatResponse {

    private String sessionId;
    private String message;
    private String intent;
    private double confidence;
    private List<String> actions;
    private Map<String, Object> context;
    private boolean requiresHumanAgent;
    private String responseType;
    private LocalDateTime timestamp;
    private long responseTimeMs;

    public ChatResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }
    
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    
    public List<String> getActions() { return actions; }
    public void setActions(List<String> actions) { this.actions = actions; }
    
    public Map<String, Object> getContext() { return context; }
    public void setContext(Map<String, Object> context) { this.context = context; }
    
    public boolean isRequiresHumanAgent() { return requiresHumanAgent; }
    public void setRequiresHumanAgent(boolean requiresHumanAgent) { this.requiresHumanAgent = requiresHumanAgent; }
    
    public String getResponseType() { return responseType; }
    public void setResponseType(String responseType) { this.responseType = responseType; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(long responseTimeMs) { this.responseTimeMs = responseTimeMs; }
}
