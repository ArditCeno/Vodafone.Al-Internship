package com.vodafone.tobi2.recommender;

import com.vodafone.tobi2.service.VodafoneService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class PlanRecommenderService {

    private static final Logger log = LoggerFactory.getLogger(PlanRecommenderService.class);

    private static final double W_SAVINGS   = 0.40;
    private static final double W_DATA_FIT  = 0.30;
    private static final double W_VOICE_FIT = 0.15;
    private static final double W_EXTRAS    = 0.15;
    private static final double OVERAGE_PENALTY = 0.20;

    private final VodafoneService vodafoneService;

    public PlanRecommenderService(VodafoneService vodafoneService) {
        this.vodafoneService = vodafoneService;
    }

    public List<PlanScore> recommend(UserUsageProfile profile) {
        List<VodafoneService.Plan> allPlans = vodafoneService.getPlans();

        List<PlanScore> scores = allPlans.stream()
            .filter(p -> !p.id().equals(profile.currentPlanId()))
            .map(plan -> scorePlan(plan, profile))
            .sorted(Comparator.comparingDouble(PlanScore::score).reversed())
            .limit(3)
            .toList();

        if (!scores.isEmpty()) {
            scores.get(0).setRecommended(true);
        }

        log.info("Recommended {} plans for user {}", scores.size(), profile.userId());
        return scores;
    }

    public PlanScore scorePlan(VodafoneService.Plan plan, UserUsageProfile profile) {
        double monthlyDataMB = profile.avgDailyDataMB() * 30;
        double dataBuffer = monthlyDataMB * 1.2;
        double dataFit = plan.dataMB() >= dataBuffer
            ? 1.0
            : Math.max(0, plan.dataMB() / dataBuffer);

        double voiceFit = plan.unlimitedCalls()
            ? 1.0
            : Math.min(plan.callMinutes() / (profile.avgMonthlyCallMinutes() * 1.1), 1.0);

        double savings = profile.currentPlanPriceLek() - plan.priceLek();
        double savingsScore = savings > 0
            ? Math.min(savings / Math.max(profile.currentPlanPriceLek(), 1), 1.0)
            : 0.0;

        double extrasScore = 0.0;
        if (profile.has5GDevice() && plan.supports5G()) extrasScore += 0.5;
        if (profile.hasRoaming() && plan.roamingIncluded()) extrasScore += 0.5;

        double penalty = (dataFit < 0.85 && profile.overageCount() > 2)
            ? OVERAGE_PENALTY
            : 0.0;

        double finalScore = (W_SAVINGS * savingsScore)
                          + (W_DATA_FIT * dataFit)
                          + (W_VOICE_FIT * voiceFit)
                          + (W_EXTRAS * extrasScore)
                          - penalty;

        String reasonKey = determineReason(savings, dataFit, profile);

        return new PlanScore(
            plan.id(), plan.name(), plan.priceLek(),
            Math.max(0, Math.min(1.0, finalScore)),
            savings,
            Math.round(dataFit * 100),
            reasonKey
        );
    }

    private String determineReason(double savings, double dataFit, UserUsageProfile p) {
        if (savings > 300) return "reason.save_money";
        if (dataFit >= 0.95 && p.overageCount() > 0) return "reason.stop_overage";
        if (p.has5GDevice()) return "reason.unlock_5g";
        if (p.hasRoaming()) return "reason.roaming_included";
        return "reason.better_fit";
    }
}
