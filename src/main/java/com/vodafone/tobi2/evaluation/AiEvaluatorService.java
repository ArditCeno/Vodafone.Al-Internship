package com.vodafone.tobi2.evaluation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AiEvaluatorService {

    private static final Logger log = LoggerFactory.getLogger(AiEvaluatorService.class);
    private final Map<String, Integer> messageCounters = new ConcurrentHashMap<>();

    public EvaluationResult evaluate(String sessionId, String userMessage, String tobiResponse) {
        int count = messageCounters.merge(sessionId, 1, Integer::sum);
        if (count % 5 != 0) return null;

        log.info("Evaluation (mock) for session {}: count={}", sessionId, count);
        return new EvaluationResult(8, 7, "TOBi2 maintains good brand tone and provides accurate information.");
    }

    public record EvaluationResult(int brandTone, int accuracy, String feedback) {}
}
