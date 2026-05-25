package com.vodafone.tobi2.agent;

public interface TobiAgent {
    String chat(String sessionId, String userId, String message, String language);
}
