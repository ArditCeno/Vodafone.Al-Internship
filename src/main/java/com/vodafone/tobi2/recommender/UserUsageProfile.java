package com.vodafone.tobi2.recommender;

public record UserUsageProfile(
    String userId,
    String currentPlanId,
    double currentPlanPriceLek,
    double avgDailyDataMB,
    double avgMonthlyCallMinutes,
    double avgMonthlySMS,
    int overageCount,
    double overagePercent,
    boolean hasRoaming,
    boolean has5GDevice,
    String zone
) {}
