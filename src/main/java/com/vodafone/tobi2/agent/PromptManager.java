package com.vodafone.tobi2.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PromptManager {

    private static final Logger log = LoggerFactory.getLogger(PromptManager.class);
    private static final String PROMPT_DIR = "classpath:prompts/";
    private final Map<String, String> promptCache = new ConcurrentHashMap<>();
    private final ResourceLoader resourceLoader;

    public PromptManager(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public String loadPrompt(String version) {
        return promptCache.computeIfAbsent(version, v -> {
            try {
                Resource resource = resourceLoader.getResource(PROMPT_DIR + "tobi2-system-prompt-" + v + ".txt");
                if (!resource.exists()) {
                    log.warn("Prompt version {} not found, loading default", v);
                    resource = resourceLoader.getResource(PROMPT_DIR + "tobi2-system-prompt-v1.txt");
                }
                return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.error("Failed to load prompt version: {}", v, e);
                return getDefaultPrompt();
            }
        });
    }

    public void reloadPrompt(String version) {
        promptCache.remove(version);
    }

    private String getDefaultPrompt() {
        return """
               You are TOBi2, Vodafone Albania's intelligent assistant.
               Always respond in a helpful, professional, and friendly manner.
               Use the Chain of Thought approach: 1) Identify intent 2) Check if tools are needed 3) Formulate response.
               When appropriate, use Vodafone branding and tone.
               """;
    }
}
