package com.vodafone.tobi.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class CrmApiService {
    
    private static final Logger log = LoggerFactory.getLogger(CrmApiService.class);
    
    private final RestTemplate restTemplate;
    
    @Value("${vodafone.api.crm.url}")
    private String crmApiUrl;
    
    @Value("${vodafone.api.crm.api-key}")
    private String crmApiKey;

    public CrmApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Get customer profile by phone number
     */
    public Map<String, Object> getCustomerProfile(String phoneNumber) {
        log.debug("Fetching customer profile: {}", phoneNumber);
        
        String url = crmApiUrl + "/customers/" + phoneNumber;
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", crmApiKey);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch customer profile for {}", phoneNumber, e);
            return Map.of("error", "Customer not found");
        }
    }

    /**
     * Get customer plan details
     */
    public Map<String, Object> getCustomerPlan(String phoneNumber) {
        log.debug("Fetching plan for: {}", phoneNumber);
        
        String url = crmApiUrl + "/customers/" + phoneNumber + "/plan";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", crmApiKey);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch plan for {}", phoneNumber, e);
            return Map.of("error", "Plan information unavailable");
        }
    }

    /**
     * Update customer information
     */
    public Map<String, Object> updateCustomer(String phoneNumber, Map<String, Object> updates) {
        log.info("Updating customer: {}", phoneNumber);
        
        String url = crmApiUrl + "/customers/" + phoneNumber;
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", crmApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(updates, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.PUT, request, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to update customer {}", phoneNumber, e);
            return Map.of("success", false, "error", "Update failed");
        }
    }
}
