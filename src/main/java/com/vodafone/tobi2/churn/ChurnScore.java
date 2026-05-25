package com.vodafone.tobi2.churn;

import java.util.List;

public record ChurnScore(
    String userId,
    double score,
    ChurnRisk risk,
    List<String> reasons,
    String personalOffer
) {}
