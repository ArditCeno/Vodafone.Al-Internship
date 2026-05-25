package com.vodafone.tobi.service;

import com.vodafone.tobi.config.WatsonxConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WatsonxService {

    private static final Logger log = LoggerFactory.getLogger(WatsonxService.class);
    private final WatsonxConfig config;

    public WatsonxService(WatsonxConfig config) {
        this.config = config;
        log.info("Watsonx Assistant configured: {}", config.getUrl());
    }

    public String createSession() {
        return "watsonx-session-" + System.currentTimeMillis();
    }
}
