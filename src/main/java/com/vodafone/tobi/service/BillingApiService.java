package com.vodafone.tobi.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class BillingApiService {
    
    private static final Logger log = LoggerFactory.getLogger(BillingApiService.class);
    
    private final RestTemplate restTemplate;
    
    @Value("${vodafone.api.billing.url}")
    private String billingApiUrl;
    
    @Value("${vodafone.api.billing.api-key}")
    private String billingApiKey;

    public BillingApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Get customer balance
     */
    public Map<String, Object> getBalance(String phoneNumber) {
        log.debug("Fetching balance for: {}", phoneNumber);
        
        String url = billingApiUrl + "/balance/" + phoneNumber;
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", billingApiKey);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch balance for {}", phoneNumber, e);
            return Map.of("balance", 0.0, "currency", "EUR", "error", "Service temporarily unavailable");
        }
    }

    /**
     * Get customer bills
     */
    public Map<String, Object> getBills(String phoneNumber) {
        log.debug("Fetching bills for: {}", phoneNumber);
        
        String url = billingApiUrl + "/bills/" + phoneNumber;
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", billingApiKey);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch bills for {}", phoneNumber, e);
            return Map.of("bills", new HashMap<>(), "error", "Service temporarily unavailable");
        }
    }

    /**
     * Pay bill
     */
    public Map<String, Object> payBill(String phoneNumber, String billId, String paymentMethod) {
        log.info("Processing bill payment: {} for {}", billId, phoneNumber);
        
        String url = billingApiUrl + "/bills/" + billId + "/pay";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", billingApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> body = Map.of(
            "phoneNumber", phoneNumber,
            "paymentMethod", paymentMethod
        );
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to pay bill {} for {}", billId, phoneNumber, e);
            return Map.of("success", false, "error", "Payment failed");
        }
    }
}
