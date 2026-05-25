package com.vodafone.tobi2.churn;

public record ChurnSignal(
    String userId,
    double avgDailyDataMB,
    double prevAvgDailyDataMB,
    int unresolvedComplaints,
    boolean askedAboutCompetitor,
    int daysSinceLastChat,
    int daysUntilContractEnd,
    boolean hasUnpaidBill
) {}
