package com.vodafone.tobi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "genesys.cloud")
public class GenesysConfig {

    private String baseUrl;
    private String clientId;
    private String clientSecret;
    private String region;
    private String queueId;

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    
    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
    
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    
    public String getQueueId() { return queueId; }
    public void setQueueId(String queueId) { this.queueId = queueId; }
}
