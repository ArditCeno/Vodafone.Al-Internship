package com.vodafone.tobi2.tests;

import com.vodafone.tobi2.evaluation.AiEvaluatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AiEvaluatorService Tests")
class AiEvaluatorServiceTest {

    private AiEvaluatorService evaluatorService;
    private String sessionId;

    @BeforeEach
    void setUp() {
        evaluatorService = new AiEvaluatorService();
        sessionId = UUID.randomUUID().toString();
    }

    @Test
    @DisplayName("testEvaluate_fifthMessage: 5 messages sent -> returns EvaluationResult")
    void testEvaluate_fifthMessage() {
        AiEvaluatorService.EvaluationResult result1 = evaluatorService.evaluate(sessionId, "msg1", "resp1");
        AiEvaluatorService.EvaluationResult result2 = evaluatorService.evaluate(sessionId, "msg2", "resp2");
        AiEvaluatorService.EvaluationResult result3 = evaluatorService.evaluate(sessionId, "msg3", "resp3");
        AiEvaluatorService.EvaluationResult result4 = evaluatorService.evaluate(sessionId, "msg4", "resp4");
        AiEvaluatorService.EvaluationResult result5 = evaluatorService.evaluate(sessionId, "msg5", "resp5");

        assertThat(result1).isNull();
        assertThat(result2).isNull();
        assertThat(result3).isNull();
        assertThat(result4).isNull();
        assertThat(result5).isNotNull();
        assertThat(result5.brandTone()).isEqualTo(8);
        assertThat(result5.accuracy()).isEqualTo(7);
        assertThat(result5.feedback()).isNotNull();
        assertThat(result5.feedback()).contains("TOBi2");
    }

    @Test
    @DisplayName("testEvaluate_nonFifthMessage: 3 messages sent -> returns null")
    void testEvaluate_nonFifthMessage() {
        AiEvaluatorService.EvaluationResult result1 = evaluatorService.evaluate(sessionId, "msg1", "resp1");
        AiEvaluatorService.EvaluationResult result2 = evaluatorService.evaluate(sessionId, "msg2", "resp2");
        AiEvaluatorService.EvaluationResult result3 = evaluatorService.evaluate(sessionId, "msg3", "resp3");

        assertThat(result1).isNull();
        assertThat(result2).isNull();
        assertThat(result3).isNull();
    }

    @Test
    @DisplayName("testEvaluate_tenthMessage: also triggers evaluation")
    void testEvaluate_tenthMessage() {
        for (int i = 1; i <= 9; i++) {
            evaluatorService.evaluate(sessionId, "msg" + i, "resp" + i);
        }
        AiEvaluatorService.EvaluationResult result10 = evaluatorService.evaluate(sessionId, "msg10", "resp10");

        assertThat(result10).isNotNull();
    }
}
