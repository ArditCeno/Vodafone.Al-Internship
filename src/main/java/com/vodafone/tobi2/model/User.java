package com.vodafone.tobi2.model;

public record User(
    Long id,
    String username,
    String fullName,
    String passwordHash,
    String role,
    String email,
    String phone,
    String userId,
    String createdAt
) {}
