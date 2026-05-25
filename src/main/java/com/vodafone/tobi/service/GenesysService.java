package com.vodafone.tobi.service;

import com.vodafone.tobi.config.GenesysConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GenesysService {

    private static final Logger log = LoggerFactory.getLogger(GenesysService.class);
    private final GenesysConfig config;

    public GenesysService(GenesysConfig config) {
        this.config = config;
        log.info("Genesys Cloud CX configured: {}", config.getBaseUrl());
    }

    public boolean isAgentAvailable() {
        return true;
    }

    public String createConversation(String sessionId, String userId, String message) {
        return "genesys-conversation-" + System.currentTimeMillis();
    }
}
