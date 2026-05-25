package com.vodafone.tobi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "watsonx.assistant")
public class WatsonxConfig {

    private String url;
    private String apikey;
    private String assistantId;
    private String version;

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    
    public String getApikey() { return apikey; }
    public void setApikey(String apikey) { this.apikey = apikey; }
    
    public String getAssistantId() { return assistantId; }
    public void setAssistantId(String assistantId) { this.assistantId = assistantId; }
    
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
}
