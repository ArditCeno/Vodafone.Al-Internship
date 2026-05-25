package com.vodafone.tobi2.churn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tobi2/admin")
public class ChurnController {

    private static final Logger log = LoggerFactory.getLogger(ChurnController.class);

    private final ChurnScoringService churnService;
    private final ChurnSignalBuilder signalBuilder;

    public ChurnController(ChurnScoringService churnService, ChurnSignalBuilder signalBuilder) {
        this.churnService = churnService;
        this.signalBuilder = signalBuilder;
    }

    @GetMapping("/churn/{userId}")
    public ResponseEntity<Map<String, Object>> getChurn(@PathVariable String userId) {
        log.info("Churn check for user {}", userId);
        ChurnSignal signal = signalBuilder.build(userId);
        ChurnScore score = churnService.calculate(signal);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("userId", score.userId());
        response.put("score", (int) Math.round(score.score()));
        response.put("risk", score.risk().name());
        response.put("reasons", score.reasons());
        response.put("personalOffer", score.personalOffer());
        response.put("signal", signal);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/churn/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        Map<String, ChurnSignal> allSignals = signalBuilder.getAllMockSignals();
        List<ChurnScore> mockScores = allSignals.values().stream()
            .map(churnService::calculate)
            .toList();

        long highCount = mockScores.stream().filter(s -> s.risk() == ChurnRisk.HIGH).count();
        long mediumCount = mockScores.stream().filter(s -> s.risk() == ChurnRisk.MEDIUM).count();
        long lowCount = mockScores.stream().filter(s -> s.risk() == ChurnRisk.LOW).count();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalUsers", mockScores.size());
        summary.put("highRiskCount", highCount);
        summary.put("mediumRiskCount", mediumCount);
        summary.put("lowRiskCount", lowCount);
        summary.put("retentionRate", 78);
        summary.put("retentionWithIntervention", 92);
        summary.put("retentionWithoutIntervention", 45);
        summary.put("users", mockScores.stream().map(s -> Map.of(
            "userId", s.userId(),
            "score", (int) Math.round(s.score()),
            "risk", s.risk().name(),
            "reasons", s.reasons(),
            "personalOffer", s.personalOffer()
        )).toList());

        return ResponseEntity.ok(summary);
    }
}
