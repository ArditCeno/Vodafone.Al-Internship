package com.vodafone.tobi.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class TopUpService {
    
    private static final Logger log = LoggerFactory.getLogger(TopUpService.class);
    
    private final RestTemplate restTemplate;
    
    @Value("${vodafone.api.provisioning.url}")
    private String provisioningApiUrl;
    
    @Value("${vodafone.api.provisioning.api-key}")
    private String provisioningApiKey;

    public TopUpService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Process top-up with voucher code
     */
    public Map<String, Object> processTopUp(String phoneNumber, String voucherCode) {
        log.info("Processing top-up for {} with voucher: {}", phoneNumber, voucherCode);
        
        String url = provisioningApiUrl + "/topup";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", provisioningApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> body = Map.of(
            "phoneNumber", phoneNumber,
            "voucherCode", voucherCode
        );
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            Map result = response.getBody();
            log.info("Top-up successful for {}: {}", phoneNumber, result);
            return result;
        } catch (Exception e) {
            log.error("Top-up failed for {}", phoneNumber, e);
            return Map.of(
                "success", false,
                "error", "Top-up failed. Please check your voucher code and try again."
            );
        }
    }

    /**
     * Get available top-up packages
     */
    public Map<String, Object> getTopUpPackages(String phoneNumber) {
        log.debug("Fetching top-up packages for: {}", phoneNumber);
        
        String url = provisioningApiUrl + "/topup/packages?phone=" + phoneNumber;
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", provisioningApiKey);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch top-up packages", e);
            return Map.of("packages", new java.util.ArrayList<>(), "error", "Service unavailable");
        }
    }

    /**
     * Activate internet package
     */
    public Map<String, Object> activateInternetPackage(String phoneNumber, String packageId) {
        log.info("Activating internet package {} for {}", packageId, phoneNumber);
        
        String url = provisioningApiUrl + "/internet/activate";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", provisioningApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> body = Map.of(
            "phoneNumber", phoneNumber,
            "packageId", packageId
        );
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to activate internet package", e);
            return Map.of("success", false, "error", "Activation failed");
        }
    }
}
