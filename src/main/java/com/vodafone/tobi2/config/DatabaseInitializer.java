package com.vodafone.tobi2.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class DatabaseInitializer {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;

    public DatabaseInitializer(JdbcTemplate jdbc, PasswordEncoder passwordEncoder) {
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        createTables();
        seedUsers();
    }

    private void createTables() {
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS tobi2_users (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(100) UNIQUE NOT NULL,
                full_name VARCHAR(200) NOT NULL,
                password_hash VARCHAR(255) NOT NULL,
                role VARCHAR(50) NOT NULL DEFAULT 'USER',
                email VARCHAR(200),
                phone VARCHAR(50),
                user_id VARCHAR(50) UNIQUE NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);

        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS tobi2_conversations (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id VARCHAR(50) NOT NULL,
                session_id VARCHAR(255) NOT NULL UNIQUE,
                title VARCHAR(500),
                started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                is_active BOOLEAN DEFAULT TRUE
            )
        """);

        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS tobi2_messages (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id VARCHAR(50) NOT NULL,
                session_id VARCHAR(255) NOT NULL,
                role VARCHAR(50) NOT NULL,
                content TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);

        log.info("Database tables initialized successfully");
    }

    private void seedUsers() {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM tobi2_users", Integer.class);
        if (count != null && count > 0) {
            log.info("Users already seeded ({} users), skipping", count);
            log.info("Delete the 'data' folder and restart to re-seed with new passwords");
            return;
        }

        log.info("Seeding default users...");

        // 20 Regular users (username, full_name, user_id)
        String[][] users = {
            {"arditceno",    "Ardit Ceno",    "VF-230510"},
            {"janazyka",     "Jana Zyka",     "VF-230511"},
            {"briseldapasha","Briselda Pasha","VF-230512"},
            {"erlaseci",     "Erla Seci",     "VF-230513"},
            {"anxhelacenaj", "Anxhela Cenaj", "VF-230514"},
            {"albiballo",    "Albi Ballo",    "VF-230515"},
            {"gledisaplaku", "Gledisa Plaku", "VF-230516"},
            {"abjolasinanaj","Abjola Sinanaj","VF-230517"},
            {"kleaceno",     "Klea Ceno",     "VF-230518"},
            {"ledjoncili",   "Ledjon Cili",   "VF-230519"},
            {"andivajvoda",  "Andi Vajvoda",  "VF-230520"},
            {"user12", "User Twelve",   "VF-230521"},
            {"user13", "User Thirteen", "VF-230522"},
            {"user14", "User Fourteen",  "VF-230523"},
            {"user15", "User Fifteen",   "VF-230524"},
            {"user16", "User Sixteen",   "VF-230525"},
            {"user17", "User Seventeen", "VF-230526"},
            {"user18", "User Eighteen",  "VF-230527"},
            {"user19", "User Nineteen",  "VF-230528"},
            {"user20", "User Twenty",    "VF-230529"}
        };

        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("       PASSWORDET E USERAVE");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        for (String[] u : users) {
            String pass = makePassword(u[1]);
            String hash = passwordEncoder.encode(pass);
            jdbc.update(
                "INSERT INTO tobi2_users (username, full_name, password_hash, role, user_id) VALUES (?, ?, ?, 'USER', ?)",
                u[0], u[1], hash, u[2]);
            log.info("  USER: {}  |  username: {}  |  password: {}", u[1], u[0], pass);
        }

        // Vodafone employees (username/email, full_name, user_id)
        String[][] employees = {
            {"ardit.ceno@vodafone",      "Ardit Ceno",      "VF-100001"},
            {"jana.zyka@vodafone",       "Jana Zyka",       "VF-100002"},
            {"klea.ceno@vodafone",       "Klea Ceno",       "VF-100003"},
            {"ledjon.cili@vodafone",     "Ledjon Cili",     "VF-100004"},
            {"juela.dyrimishi@vodafone", "Juela Dyrimishi", "VF-100005"},
            {"joana.mucaj@vodafone",     "Joana Mucaj",     "VF-100006"},
            {"denata.mata@vodafone",     "Denata Mata",     "VF-100007"},
            {"kristiano.zyka@vodafone",  "Kristiano Zyka",  "VF-100008"}
        };

        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("     PASSWORDET E PUNONJESVE");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        for (String[] e : employees) {
            String pass = makePassword(e[1]);
            String hash = passwordEncoder.encode(pass);
            jdbc.update(
                "INSERT INTO tobi2_users (username, full_name, password_hash, role, email, user_id) VALUES (?, ?, ?, 'VODAFONE_EMPLOYEE', ?, ?)",
                e[0], e[1], hash, e[0], e[2]);
            log.info("  PUNONJES: {}  |  email: {}  |  password: {}", e[1], e[0], pass);
        }

        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("Seeded {} users + {} employees", users.length, employees.length);
    }

    private String makePassword(String fullName) {
        return switch (fullName) {
            case "Ardit Ceno"      -> "ardit123";
            case "Jana Zyka"       -> "jana123";
            case "Briselda Pasha"  -> "briselda123";
            case "Erla Seci"       -> "erla123";
            case "Anxhela Cenaj"   -> "anxhela123";
            case "Albi Ballo"      -> "albi123";
            case "Gledisa Plaku"   -> "gledisa123";
            case "Abjola Sinanaj"  -> "abjola123";
            case "Klea Ceno"       -> "klea123";
            case "Ledjon Cili"     -> "ledjon123";
            case "Andi Vajvoda"    -> "andi123";
            default -> {
                String first = fullName.split(" ")[0].toLowerCase();
                yield first + "123";
            }
        };
    }
}
