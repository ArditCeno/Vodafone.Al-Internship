package com.vodafone.tobi2.controller;

import com.vodafone.tobi2.model.ChangePasswordRequest;
import com.vodafone.tobi2.model.LoginRequest;
import com.vodafone.tobi2.model.RegisterRequest;
import com.vodafone.tobi2.service.ConversationService;
import com.vodafone.tobi2.service.JwtService;
import com.vodafone.tobi2.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tobi2/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final JwtService jwtService;
    private final ConversationService conversationService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserService userService, JwtService jwtService,
                          ConversationService conversationService,
                          PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.conversationService = conversationService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String token = userService.login(request.username(), request.password());
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "authenticated", false,
                "error", "Kredencialet e gabuara!"
            ));
        }

        var userOpt = userService.findByUsername(request.username());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "authenticated", false,
                "error", "Kredencialet e gabuara!"
            ));
        }

        var user = userOpt.get();
        var conversations = conversationService.getConversations(user.userId());

        log.info("User logged in: {} ({})", user.fullName(), user.userId());

        return ResponseEntity.ok(Map.of(
            "authenticated", true,
            "token", token,
            "userId", user.userId(),
            "fullName", user.fullName(),
            "role", user.role(),
            "email", user.email() != null ? user.email() : "",
            "conversations", conversations.stream().map(c -> Map.of(
                "sessionId", c.sessionId(),
                "title", c.title() != null ? c.title() : "Bisedë e re",
                "startedAt", c.startedAt(),
                "active", c.isActive()
            )).toList(),
            "message", "Autentikimi u krye me sukses!"
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (request.username() == null || request.username().isBlank() ||
            request.fullName() == null || request.fullName().isBlank() ||
            request.password() == null || request.password().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Të gjitha fushat janë të detyrueshme!"
            ));
        }

        if (userService.existsByUsername(request.username())) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Ky përdorues ekziston tashmë!"
            ));
        }

        if (request.password().length() < 6) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Fjalëkalimi duhet të ketë të paktën 6 karaktere!"
            ));
        }

        try {
            var user = userService.register(request.username(), request.fullName(), request.password());
            String token = jwtService.generateToken(user.userId(), user.role(), user.fullName());

            log.info("New user registered: {} ({})", user.fullName(), user.userId());

            return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "token", token,
                "userId", user.userId(),
                "fullName", user.fullName(),
                "role", user.role(),
                "message", "Regjistrimi u krye me sukses!"
            ));
        } catch (Exception e) {
            log.error("Registration failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Regjistrimi dështoi. Provo përsëri."
            ));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ChangePasswordRequest request) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token i pavlefshëm!"));
        }

        String token = authHeader.substring(7);
        if (!jwtService.isValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token i skaduar!"));
        }

        String userId = jwtService.getUserId(token);

        if (request.newPassword() == null || request.newPassword().length() < 6) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Fjalëkalimi i ri duhet të ketë të paktën 6 karaktere!"
            ));
        }

        boolean changed = userService.changePassword(userId, request.oldPassword(), request.newPassword());
        if (!changed) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Fjalëkalimi i vjetër është i gabuar!"
            ));
        }

        log.info("Password changed for userId={}", userId);
        return ResponseEntity.ok(Map.of("message", "Fjalëkalimi u ndryshua me sukses!"));
    }

    @PostMapping("/hash-password")
    public ResponseEntity<?> hashPassword(@RequestBody Map<String, String> body) {
        String password = body.get("password");
        if (password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password is required"));
        }
        String hash = passwordEncoder.encode(password);
        return ResponseEntity.ok(Map.of("password", password, "hash", hash));
    }

    @PostMapping("/admin/reset-password")
    public ResponseEntity<?> adminResetPassword(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String newPassword = body.get("newPassword");
        if (username == null || username.isBlank() || newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "username dhe newPassword janë të detyrueshme"));
        }
        var userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User nuk u gjet"));
        }
        String newHash = passwordEncoder.encode(newPassword);
        userService.updatePassword(userOpt.get().userId(), newHash);
        log.info("Admin reset password for username={}", username);
        return ResponseEntity.ok(Map.of("message", "Fjalëkalimi u ndryshua me sukses për " + username));
    }
}
