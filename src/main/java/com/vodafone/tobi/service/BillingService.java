package com.vodafone.tobi.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class BillingService {

    private static final Logger log = LoggerFactory.getLogger(BillingService.class);
    private final Random random = new Random();
    
    private final Map<String, UserBillingData> simulatedData = new HashMap<>();

    public BillingService() {
        initializeSimulatedData();
    }

    private void initializeSimulatedData() {
        simulatedData.put("INTERN_DEMO_001", new UserBillingData("2,450", "1,250", "15/05/2026", "Unlimited M"));
        simulatedData.put("USER_002", new UserBillingData("850", "3,100", "20/05/2026", "Unlimited L"));
        simulatedData.put("USER_003", new UserBillingData("120", "950", "10/05/2026", "Smart S"));
    }

    public String getBalance(String userId) {
        UserBillingData data = simulatedData.getOrDefault(userId, getDefaultData());
        return data.balance + " Lek";
    }

    public String getBill(String userId) {
        UserBillingData data = simulatedData.getOrDefault(userId, getDefaultData());
        return data.billAmount + " Lek (Due: " + data.dueDate + ")";
    }

    public String getPlan(String userId) {
        UserBillingData data = simulatedData.getOrDefault(userId, getDefaultData());
        return data.plan;
    }

    public int getRemainingDays(String userId) {
        UserBillingData data = simulatedData.getOrDefault(userId, getDefaultData());
        LocalDate dueDate = LocalDate.parse("2026-" + data.dueDate.substring(3,5) + "-" + data.dueDate.substring(0,2));
        return (int) (java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), dueDate) + (int)(Math.random() * 10));
    }

    public String getDataUsage(String userId) {
        int used = 5 + random.nextInt(15);
        int total = 20;
        return used + "GB / " + total + "GB";
    }

    private UserBillingData getDefaultData() {
        return new UserBillingData("1,000", "1,500", "15/05/2026", "Unlimited M");
    }

    private static class UserBillingData {
        String balance;
        String billAmount;
        String dueDate;
        String plan;

        UserBillingData(String balance, String billAmount, String dueDate, String plan) {
            this.balance = balance;
            this.billAmount = billAmount;
            this.dueDate = dueDate;
            this.plan = plan;
        }
    }
}
