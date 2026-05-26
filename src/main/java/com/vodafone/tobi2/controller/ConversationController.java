package com.vodafone.tobi2.controller;

import com.vodafone.tobi2.service.ConversationService;
import com.vodafone.tobi2.service.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tobi2/conversations")
public class ConversationController {

    private static final Logger log = LoggerFactory.getLogger(ConversationController.class);

    private final ConversationService conversationService;
    private final JwtService jwtService;

    public ConversationController(ConversationService conversationService, JwtService jwtService) {
        this.conversationService = conversationService;
        this.jwtService = jwtService;
    }

    @GetMapping
    public ResponseEntity<?> getConversations(
            @RequestHeader("Authorization") String authHeader) {
        var userId = extractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Token i pavlefshëm!"));
        }

        var conversations = conversationService.getConversations(userId);
        log.debug("Returning {} conversations for userId={}", conversations.size(), userId);

        return ResponseEntity.ok(Map.of(
            "conversations", conversations.stream().map(c -> Map.of(
                "sessionId", c.sessionId(),
                "title", c.title() != null ? c.title() : "Bisedë e re",
                "startedAt", c.startedAt(),
                "active", c.isActive()
            )).toList()
        ));
    }

    @GetMapping("/{sessionId}/messages")
    public ResponseEntity<?> getMessages(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String sessionId) {
        var userId = extractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Token i pavlefshëm!"));
        }

        if (!conversationService.sessionBelongsToUser(sessionId, userId)) {
            log.warn("User {} tried to access session {} not belonging to them", userId, sessionId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Nuk keni akses në këtë bisedë!"));
        }

        var messages = conversationService.getMessages(sessionId);
        return ResponseEntity.ok(Map.of(
            "messages", messages.stream().map(m -> Map.of(
                "role", m.role(),
                "content", m.content(),
                "createdAt", m.createdAt()
            )).toList()
        ));
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<?> deleteConversation(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String sessionId) {
        var userId = extractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Token i pavlefshëm!"));
        }

        if (!conversationService.sessionBelongsToUser(sessionId, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Nuk keni akses në këtë bisedë!"));
        }

        conversationService.endSession(sessionId);
        return ResponseEntity.ok(Map.of("message", "Biseda u arkivua!"));
    }

    private String extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        String token = authHeader.substring(7);
        if (!jwtService.isValid(token)) return null;
        return jwtService.getUserId(token);
    }
}
