package com.vodafone.tobi2.service;

import com.vodafone.tobi2.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final UserRowMapper rowMapper = new UserRowMapper();

    public UserService(JdbcTemplate jdbc, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public Optional<User> findByUsername(String username) {
        var users = jdbc.query(
            "SELECT * FROM tobi2_users WHERE username = ?", rowMapper, username);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public Optional<User> findByUserId(String userId) {
        var users = jdbc.query(
            "SELECT * FROM tobi2_users WHERE user_id = ?", rowMapper, userId);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public User register(String username, String fullName, String rawPassword) {
        String hash = passwordEncoder.encode(rawPassword);
        String userId = generateUserId();
        jdbc.update(
            "INSERT INTO tobi2_users (username, full_name, password_hash, role, user_id) VALUES (?, ?, ?, 'USER', ?)",
            username, fullName, hash, userId);
        log.info("Registered new user: username={}, userId={}", username, userId);
        return findByUsername(username).orElseThrow();
    }

    public String login(String username, String rawPassword) {
        var userOpt = findByUsername(username);
        if (userOpt.isEmpty()) {
            log.warn("Login failed: user not found: {}", username);
            return null;
        }
        User user = userOpt.get();
        if (!passwordEncoder.matches(rawPassword, user.passwordHash())) {
            log.warn("Login failed: wrong password for: {}", username);
            return null;
        }
        return jwtService.generateToken(user.userId(), user.role(), user.fullName());
    }

    public boolean changePassword(String userId, String oldPassword, String newPassword) {
        var userOpt = findByUserId(userId);
        if (userOpt.isEmpty()) return false;
        User user = userOpt.get();
        if (!passwordEncoder.matches(oldPassword, user.passwordHash())) return false;
        String newHash = passwordEncoder.encode(newPassword);
        jdbc.update("UPDATE tobi2_users SET password_hash = ? WHERE user_id = ?", newHash, userId);
        log.info("Password changed for userId={}", userId);
        return true;
    }

    public boolean existsByUsername(String username) {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM tobi2_users WHERE username = ?", Integer.class, username);
        return count != null && count > 0;
    }

    public void updatePassword(String userId, String newHash) {
        jdbc.update("UPDATE tobi2_users SET password_hash = ? WHERE user_id = ?", newHash, userId);
        log.info("Password updated for userId={}", userId);
    }

    private String generateUserId() {
        Integer maxId = jdbc.queryForObject(
            "SELECT MAX(CAST(SUBSTRING(user_id, 4) AS INTEGER)) FROM tobi2_users", Integer.class);
        int next = (maxId == null ? 230510 : maxId + 1);
        return "VF-" + next;
    }

    private static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new User(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("full_name"),
                rs.getString("password_hash"),
                rs.getString("role"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("user_id"),
                rs.getString("created_at")
            );
        }
    }
}
