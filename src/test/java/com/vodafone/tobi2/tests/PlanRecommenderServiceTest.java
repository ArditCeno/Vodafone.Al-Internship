package com.vodafone.tobi2.tests;

import com.vodafone.tobi2.recommender.PlanRecommenderService;
import com.vodafone.tobi2.recommender.PlanScore;
import com.vodafone.tobi2.recommender.UserUsageProfile;
import com.vodafone.tobi2.service.VodafoneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PlanRecommenderService Tests")
class PlanRecommenderServiceTest {

    private PlanRecommenderService service;

    @BeforeEach
    void setUp() {
        service = new PlanRecommenderService(new VodafoneService());
    }

    @Test
    @DisplayName("recommend: heavy user gets Unlimited Max as top pick")
    void testHeavyUserGetsUnlimitedMax() {
        var profile = new UserUsageProfile(
            "user1", "smart-s", 400,
            900, 1000, 500,
            5, 0.5,
            true, true, "Tirane"
        );

        List<PlanScore> results = service.recommend(profile);

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).planName()).isEqualTo("Unlimited Max");
        assertThat(results.get(0).recommended()).isTrue();
        assertThat(results.get(0).score()).isGreaterThan(0);
    }

    @Test
    @DisplayName("recommend: light user gets Smart S as top pick")
    void testLightUserGetsSmartS() {
        var profile = new UserUsageProfile(
            "user2", "unlimited-max", 1500,
            100, 100, 50,
            0, 0.0,
            false, false, "Korce"
        );

        List<PlanScore> results = service.recommend(profile);

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).recommended()).isTrue();
        assertThat(results.get(0).monthlySavingsLek()).isPositive();
    }

    @Test
    @DisplayName("recommend: returns max 3 results")
    void testMaxThreeResults() {
        var profile = new UserUsageProfile(
            "user3", "none", 0,
            200, 200, 100,
            1, 0.1,
            true, false, "Durres"
        );

        List<PlanScore> results = service.recommend(profile);

        assertThat(results).hasSizeLessThanOrEqualTo(3);
    }

    @Test
    @DisplayName("recommend: excludes current plan from results")
    void testExcludesCurrentPlan() {
        var profile = new UserUsageProfile(
            "user4", "unlimited-max", 1500,
            100, 50, 20,
            0, 0.0,
            false, false, "Vlore"
        );

        List<PlanScore> results = service.recommend(profile);

        assertThat(results).noneMatch(ps -> ps.planId().equals("unlimited-max"));
    }

    @Test
    @DisplayName("recommend: scores are between 0 and 1")
    void testScoresInRange() {
        var profile = new UserUsageProfile(
            "user5", "unlimited-l", 1000,
            200, 300, 100,
            1, 0.1,
            true, true, "Shkoder"
        );

        List<PlanScore> results = service.recommend(profile);

        for (PlanScore ps : results) {
            assertThat(ps.score()).isBetween(0.0, 1.0);
        }
    }

    @Test
    @DisplayName("recommend: heavy overage user triggers stop_overage reason")
    void testOverageReason() {
        var profile = new UserUsageProfile(
            "user6", "smart-s", 400,
            200, 100, 50,
            5, 0.8,
            false, false, "Elbasan"
        );

        List<PlanScore> results = service.recommend(profile);

        assertThat(results).isNotEmpty();
    }

    @Test
    @DisplayName("recommend: 5G user gets reason.unlock_5g when applicable")
    void test5GUserGets5GReason() {
        var profile = new UserUsageProfile(
            "user7", "smart-s", 400,
            100, 50, 20,
            0, 0.0,
            false, true, "Tirane"
        );

        List<PlanScore> results = service.recommend(profile);

        PlanScore top = results.get(0);
        assertThat(top.reasonKey()).isEqualTo("reason.unlock_5g");
    }
}
