package com.vodafone.tobi2.recommender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tobi2/plan")
public class PlanRecommenderController {

    private static final Logger log = LoggerFactory.getLogger(PlanRecommenderController.class);

    private final PlanRecommenderService recommenderService;

    public PlanRecommenderController(PlanRecommenderService recommenderService) {
        this.recommenderService = recommenderService;
    }

    @PostMapping("/recommend")
    public ResponseEntity<Map<String, Object>> recommend(@RequestBody UserUsageProfile profile) {
        log.info("Plan recommendation request for user {}", profile.userId());
        List<PlanScore> scores = recommenderService.recommend(profile);

        Map<String, Object> response = Map.of(
            "userId", profile.userId(),
            "recommendations", scores.stream().map(this::toMap).toList()
        );
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> toMap(PlanScore ps) {
        return Map.of(
            "planId", ps.planId(),
            "planName", ps.planName(),
            "priceLek", ps.priceLek(),
            "score", Math.round(ps.score() * 1000) / 1000.0,
            "monthlySavingsLek", Math.round(ps.monthlySavingsLek()),
            "fitPercent", (int) ps.fitPercent(),
            "reasonKey", ps.reasonKey(),
            "recommended", ps.recommended()
        );
    }
}
