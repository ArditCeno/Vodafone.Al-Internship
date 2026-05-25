package com.vodafone.tobi2.recommender;

public class PlanScore {

    private final String planId;
    private final String planName;
    private final double priceLek;
    private final double score;
    private final double monthlySavingsLek;
    private final double fitPercent;
    private final String reasonKey;
    private boolean recommended;

    public PlanScore(String planId, String planName, double priceLek, double score,
                     double monthlySavingsLek, double fitPercent, String reasonKey) {
        this.planId = planId;
        this.planName = planName;
        this.priceLek = priceLek;
        this.score = score;
        this.monthlySavingsLek = monthlySavingsLek;
        this.fitPercent = fitPercent;
        this.reasonKey = reasonKey;
        this.recommended = false;
    }

    public String planId() { return planId; }
    public String planName() { return planName; }
    public double priceLek() { return priceLek; }
    public double score() { return score; }
    public double monthlySavingsLek() { return monthlySavingsLek; }
    public double fitPercent() { return fitPercent; }
    public String reasonKey() { return reasonKey; }
    public boolean recommended() { return recommended; }
    public void setRecommended(boolean recommended) { this.recommended = recommended; }
}
