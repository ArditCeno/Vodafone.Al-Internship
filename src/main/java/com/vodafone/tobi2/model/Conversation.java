package com.vodafone.tobi2.model;

public record Conversation(
    Long id,
    String userId,
    String sessionId,
    String title,
    String startedAt,
    boolean isActive
) {}
