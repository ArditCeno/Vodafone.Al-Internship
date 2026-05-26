package com.vodafone.tobi2.model;

public record ChatMessage(
    Long id,
    String userId,
    String sessionId,
    String role,
    String content,
    String createdAt
) {}
