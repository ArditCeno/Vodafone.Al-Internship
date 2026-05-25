package com.vodafone.tobi2.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConversationMetricsService {

    private static final Logger log = LoggerFactory.getLogger(ConversationMetricsService.class);

    private final Counter conversationCounter;
    private final Timer responseTimer;
    private final Map<String, Long> conversationCosts = new ConcurrentHashMap<>();

    public ConversationMetricsService(MeterRegistry meterRegistry) {
        this.conversationCounter = Counter.builder("tobi2.conversations.total")
                .description("Total number of conversations")
                .register(meterRegistry);
        this.responseTimer = Timer.builder("tobi2.response.time")
                .description("Response time for TOBi2")
                .register(meterRegistry);
    }

    public void recordConversation(String sessionId, long responseTimeMs) {
        conversationCounter.increment();
        responseTimer.record(java.time.Duration.ofMillis(responseTimeMs));
        log.info("Telemetry: session={}, responseTime={}ms", sessionId, responseTimeMs);
    }

    public void recordToolUsage(String toolName) {
        log.info("Telemetry: toolUsed={}", toolName);
    }

    public void recordCost(String sessionId, double costLek) {
        conversationCosts.merge(sessionId, (long) (costLek * 100), Long::sum);
    }

    public double getCostPerConversation(String sessionId) {
        Long cost = conversationCosts.get(sessionId);
        return cost != null ? cost / 100.0 : 0.0;
    }
}
