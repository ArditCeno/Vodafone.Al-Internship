/*
package com.vodafone.tobi2.tests;

import com.vodafone.tobi2.churn.ChurnRisk;
import com.vodafone.tobi2.churn.ChurnScore;
import com.vodafone.tobi2.churn.ChurnScoringService;
import com.vodafone.tobi2.churn.ChurnSignal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ChurnScoringService Tests")
class ChurnScoringServiceTest {

    private ChurnScoringService service;

    @BeforeEach
    void setUp() {
        service = new ChurnScoringService();
    }

    @Test
    @DisplayName("High churn: data drop + complaints + competitor + unpaid bill")
    void testHighChurn() {
        var signal = new ChurnSignal(
            "user1", 100, 900, 3, true, 20, 10, true);

        ChurnScore result = service.calculate(signal);

        assertThat(result.risk()).isEqualTo(ChurnRisk.HIGH);
        assertThat(result.score()).isGreaterThanOrEqualTo(65);
        assertThat(result.reasons()).contains("data_drop", "unresolved_complaints",
            "competitor_inquiry", "inactive_14d", "contract_ending_soon", "unpaid_bill");
    }

    @Test
    @DisplayName("Medium churn: some signals but not critical")
    void testMediumChurn() {
        var signal = new ChurnSignal(
            "user2", 400, 600, 1, false, 10, 40, false);

        ChurnScore result = service.calculate(signal);

        assertThat(result.risk()).isEqualTo(ChurnRisk.MEDIUM);
        assertThat(result.score()).isBetween(35.0, 64.0);
        assertThat(result.reasons()).isNotEmpty();
    }

    @Test
    @DisplayName("Low churn: healthy customer")
    void testLowChurn() {
        var signal = new ChurnSignal(
            "user3", 800, 850, 0, false, 2, 120, false);

        ChurnScore result = service.calculate(signal);

        assertThat(result.risk()).isEqualTo(ChurnRisk.LOW);
        assertThat(result.score()).isLessThan(35);
    }

    @Test
    @DisplayName("Score is between 0 and 100")
    void testScoreRange() {
        var highSignal = new ChurnSignal(
            "u1", 0, 1000, 5, true, 30, 5, true);
        var lowSignal = new ChurnSignal(
            "u2", 500, 510, 0, false, 1, 200, false);

        ChurnScore high = service.calculate(highSignal);
        ChurnScore low = service.calculate(lowSignal);

        assertThat(high.score()).isBetween(0.0, 100.0);
        assertThat(low.score()).isBetween(0.0, 100.0);
    }

    @Test
    @DisplayName("getPersonalOffer: score >= 80 offers free month")
    void testOfferCritical() {
        assertThat(service.getPersonalOffer(85)).contains("falas");
        assertThat(service.getPersonalOffer(95)).contains("falas");
    }

    @Test
    @DisplayName("getPersonalOffer: score >= 65 offers free upgrade")
    void testOfferHigh() {
        assertThat(service.getPersonalOffer(65)).contains("Upgrade");
        assertThat(service.getPersonalOffer(75)).contains("Upgrade");
    }

    @Test
    @DisplayName("getPersonalOffer: score >= 35 offers bonus GB")
    void testOfferMedium() {
        assertThat(service.getPersonalOffer(35)).contains("GB");
        assertThat(service.getPersonalOffer(50)).contains("GB");
    }

    @Test
    @DisplayName("getPersonalOffer: score < 35 returns empty offer")
    void testOfferLow() {
        assertThat(service.getPersonalOffer(20)).isEmpty();
    }

    @Test
    @DisplayName("No data drop calculates correctly")
    void testNoDataDrop() {
        var signal = new ChurnSignal(
            "user4", 800, 800, 0, false, 1, 180, false);

        ChurnScore result = service.calculate(signal);

        assertThat(result.risk()).isEqualTo(ChurnRisk.LOW);
        assertThat(result.reasons()).isEmpty();
    }
}
*/
