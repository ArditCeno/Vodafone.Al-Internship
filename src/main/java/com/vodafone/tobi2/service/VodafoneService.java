package com.vodafone.tobi2.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VodafoneService {

    private static final Logger log = LoggerFactory.getLogger(VodafoneService.class);

    public record Plan(
        String id, String name, double priceLek, int dataMB,
        int callMinutes, boolean unlimitedCalls, boolean supports5G,
        boolean roamingIncluded, boolean includesSocialMedia
    ) {}

    private static final List<Plan> PLANS = List.of(
        new Plan("unlimited-max", "Unlimited Max", 1500, 99999, 0, true, true, true, true),
        new Plan("unlimited-l", "Unlimited L", 1000, 51200, 0, true, true, true, true),
        new Plan("unlimited-m", "Unlimited M", 700, 20480, 0, true, true, false, false),
        new Plan("smart-s", "Smart S", 400, 5120, 500, false, false, false, false)
    );

    public List<Plan> getPlans() {
        return PLANS;
    }

    public String getBalance(String userId) {
        log.info("getBalance for {}", userId);
        return "2,450 Lek";
    }

    public String getPlan(String userId) {
        log.info("getPlan for {}", userId);
        return "Unlimited Max";
    }

    public String getDataUsage(String userId) {
        log.info("getDataUsage for {}", userId);
        return "12GB / 20GB";
    }

    public String getRoamingCost(String country) {
        log.info("getRoamingCost for {}", country);
        return "0.05 EUR/min";
    }

    public String checkNetworkStatus(String postcode) {
        log.info("checkNetworkStatus for {}", postcode);
        return "Rrjeti ne " + postcode + " eshte ne rregull.";
    }

    public String getBill(String userId) {
        log.info("getBill for {}", userId);
        return "1,250 Lek (pagesa deri me 27/05/2026)";
    }

    public String getAvailablePlans() {
        return """
               Planet me te mira:
               1. Unlimited Max - 1500 Lek/muaj
               2. Unlimited L - 1000 Lek/muaj
               3. Unlimited M - 700 Lek/muaj
               4. Smart S - 400 Lek/muaj""";
    }
}
