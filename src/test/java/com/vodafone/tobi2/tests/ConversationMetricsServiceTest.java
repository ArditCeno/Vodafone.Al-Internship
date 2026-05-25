package com.vodafone.tobi2.tests;

import com.vodafone.tobi2.monitoring.ConversationMetricsService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ConversationMetricsService Tests")
class ConversationMetricsServiceTest {

    private MeterRegistry meterRegistry;
    private ConversationMetricsService metricsService;
    private String sessionId;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metricsService = new ConversationMetricsService(meterRegistry);
        sessionId = UUID.randomUUID().toString();
    }

    @Test
    @DisplayName("testRecordConversation: record conversation -> counter incremented")
    void testRecordConversation() {
        Counter counter = meterRegistry.find("tobi2.conversations.total").counter();
        double initialCount = counter != null ? counter.count() : 0.0;

        metricsService.recordConversation(sessionId, 150);

        Counter updatedCounter = meterRegistry.find("tobi2.conversations.total").counter();
        assertThat(updatedCounter).isNotNull();
        assertThat(updatedCounter.count()).isEqualTo(initialCount + 1.0);
    }

    @Test
    @DisplayName("testRecordCost: record cost -> retrievable via getCostPerConversation()")
    void testRecordCost() {
        String testSession = "test-session-cost";
        double initialCost = metricsService.getCostPerConversation(testSession);
        assertThat(initialCost).isEqualTo(0.0);

        metricsService.recordCost(testSession, 50.5);
        double costAfterFirst = metricsService.getCostPerConversation(testSession);
        assertThat(costAfterFirst).isEqualTo(50.5);

        metricsService.recordCost(testSession, 25.25);
        double totalCost = metricsService.getCostPerConversation(testSession);
        assertThat(totalCost).isEqualTo(75.75);
    }
}
