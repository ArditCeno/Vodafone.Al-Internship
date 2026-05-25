package com.vodafone.tobi.controller;

import com.vodafone.tobi.model.ChatRequest;
import com.vodafone.tobi.model.ChatResponse;
import com.vodafone.tobi.model.ConversationSession;
import com.vodafone.tobi.service.TobiService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tobi")
public class ChatController {

    private final TobiService tobiService;

    public ChatController(TobiService tobiService) {
        this.tobiService = tobiService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = tobiService.chat(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("TOBi Albania Backend - UP");
    }

    @GetMapping("/conversation/start")
    public ResponseEntity<ConversationSession> startConversation(
            @RequestParam String userId,
            @RequestParam(defaultValue = "web") String channel,
            @RequestParam(defaultValue = "sq") String language) {
        ConversationSession session = tobiService.createSession(userId, channel, language);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/conversation/{sessionId}")
    public ResponseEntity<ConversationSession> getConversation(@PathVariable String sessionId) {
        ConversationSession session = tobiService.getSession(sessionId);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(session);
    }

    @GetMapping("/conversation/end/{sessionId}")
    public ResponseEntity<String> endConversation(@PathVariable String sessionId) {
        tobiService.endSession(sessionId);
        return ResponseEntity.ok("Session ended");
    }

    @PostMapping("/session/end/{sessionId}")
    public ResponseEntity<String> endSession(@PathVariable String sessionId) {
        tobiService.endSession(sessionId);
        return ResponseEntity.ok("Session ended");
    }
}
