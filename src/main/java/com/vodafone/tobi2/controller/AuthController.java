package com.vodafone.tobi2.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/tobi2/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String loginType = body.getOrDefault("loginType", "regular");

        if ("regular".equals(loginType)) {
            if (("Ardit Ceno".equals(username) || "0694770832".equals(username)) && "password123".equals(password)) {
                String mockJwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.mockToken-" + UUID.randomUUID();
                return ResponseEntity.ok(Map.of(
                        "authenticated", true,
                        "token", mockJwtToken,
                        "userId", "VF-230510",
                        "fullName", "Ardit Ceno",
                        "role", "USER",
                        "message", "Autentikimi u krye me sukses!"
                ));
            }
        } else if ("vodafone".equals(loginType)) {
            if ("ardit.ceno@vodafone".equals(username) && "vodafone123".equals(password)) {
                String mockJwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.mockToken-" + UUID.randomUUID();
                return ResponseEntity.ok(Map.of(
                        "authenticated", true,
                        "token", mockJwtToken,
                        "userId", "VF-230510",
                        "fullName", "Ardit Ceno",
                        "role", "VODAFONE_EMPLOYEE",
                        "message", "Autentikimi u krye me sukses!"
                ));
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "authenticated", false,
                "error", "Kredencialet e gabuara!"
        ));
    }
}
