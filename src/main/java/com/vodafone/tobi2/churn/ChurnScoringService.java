package com.vodafone.tobi2.churn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChurnScoringService {

    private static final Logger log = LoggerFactory.getLogger(ChurnScoringService.class);

    public ChurnScore calculate(ChurnSignal s) {
        double score = 0;
        List<String> reasons = new ArrayList<>();

        double drop = (s.prevAvgDailyDataMB() - s.avgDailyDataMB())
                      / Math.max(s.prevAvgDailyDataMB(), 1);
        double dataPoints = Math.min(drop * 40, 25);
        if (dataPoints >= 10) {
            reasons.add("data_drop");
        }
        score += dataPoints;

        double complaintPoints = Math.min(s.unresolvedComplaints() * 10, 20);
        if (s.unresolvedComplaints() >= 2) {
            reasons.add("unresolved_complaints");
        }
        score += complaintPoints;

        if (s.askedAboutCompetitor()) {
            score += 20;
            reasons.add("competitor_inquiry");
        }

        if (s.daysSinceLastChat() > 14) {
            score += 15;
            reasons.add("inactive_14d");
        } else if (s.daysSinceLastChat() > 7) {
            score += 8;
            reasons.add("inactive_7d");
        }

        if (s.daysUntilContractEnd() < 30) {
            score += 10;
            reasons.add("contract_ending_soon");
        } else if (s.daysUntilContractEnd() < 60) {
            score += 5;
        }

        if (s.hasUnpaidBill()) {
            score += 10;
            reasons.add("unpaid_bill");
        }

        ChurnRisk risk = score >= 65 ? ChurnRisk.HIGH
                       : score >= 35 ? ChurnRisk.MEDIUM
                       : ChurnRisk.LOW;

        String offer = getPersonalOffer(score);

        log.info("Churn score for {}: {}/100 (risk={}, reasons={})", s.userId(), score, risk, reasons);

        return new ChurnScore(s.userId(), score, risk, reasons, offer);
    }

    public String getPersonalOffer(double score) {
        if (score >= 80) {
            return "1 muaj falas + 50% zbritje për 3 muajt e ardhshëm";
        } else if (score >= 65) {
            return "Upgrade falas në planin Plus për 2 muaj";
        } else if (score >= 35) {
            return "5 GB bonus këtë muaj si shenjë mirënjohjeje";
        }
        return "";
    }
}
