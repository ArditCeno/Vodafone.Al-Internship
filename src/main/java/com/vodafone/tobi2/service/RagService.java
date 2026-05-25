package com.vodafone.tobi2.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);

    private final Map<String, String> knowledgeBase = new LinkedHashMap<>();

    public RagService() {
        loadLocalKnowledge();
    }

    private void loadLocalKnowledge() {
        knowledgeBase.put("unlimited-max",
                "Unlimited Max: 1500 Lek/month. Includes unlimited 5G data, unlimited calls & SMS to all networks in Albania, " +
                "plus 10GB roaming in EU. Activation: dial *123# or My Vodafone app.");
        knowledgeBase.put("unlimited-l",
                "Unlimited L: 1000 Lek/month. Includes 50GB 5G data, unlimited social media (TikTok, Instagram, Facebook), " +
                "unlimited calls & SMS. Activation: dial *123#.");
        knowledgeBase.put("unlimited-m",
                "Unlimited M: 700 Lek/month. Includes 20GB 5G data, unlimited calls & SMS. Activation: dial *123#.");
        knowledgeBase.put("smart-s",
                "Smart S: 400 Lek/month. Includes 5GB 4G data, 500 minutes, 500 SMS. Activation: dial *123#.");
        knowledgeBase.put("roaming-eu",
                "Roaming in EU: Standard rates apply. 0.05 EUR/min calls, 0.02 EUR/SMS, 0.05 EUR/MB data. " +
                "Special EU roaming packages available from 300 Lek for 5GB.");
        knowledgeBase.put("5g-info",
                "Vodafone Albania 5G: Available in Tirana, Durres, Vlore, Shkoder, Elbasan, Korce. " +
                "5G is included in all Unlimited plans at no extra cost. Max speed: 1Gbps.");
        knowledgeBase.put("topup-methods",
                "Top-up methods: 1) Buy voucher at any Vodafone store or partner 2) My Vodafone app (credit/debit card) " +
                "3) Bank transfer 4) Dial *140*code# to use voucher. Minimum top-up: 100 Lek.");
    }

    public List<String> search(String query) {
        if (query == null || query.isBlank()) return List.of();
        String q = query.toLowerCase();
        List<String> results = new ArrayList<>();
        for (var entry : knowledgeBase.entrySet()) {
            if (entry.getKey().contains(q) || entry.getValue().toLowerCase().contains(q)) {
                results.add(entry.getValue());
            }
        }
        if (results.isEmpty()) {
            String fuzzy = fuzzySearch(q);
            if (fuzzy != null) results.add(fuzzy);
        }
        log.debug("RAG search for '{}' found {} results", query, results.size());
        return results;
    }

    private String fuzzySearch(String query) {
        String normalized = query.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        for (var entry : knowledgeBase.entrySet()) {
            String key = entry.getKey().toLowerCase();
            String val = entry.getValue().toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
            if (key.contains(normalized) || val.contains(normalized)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
