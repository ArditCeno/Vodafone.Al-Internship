package com.vodafone.tobi2.tests;

import com.vodafone.tobi2.agent.PromptManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PromptManager Tests")
class PromptManagerTest {

    private ResourceLoader resourceLoader;
    private PromptManager promptManager;

    @BeforeEach
    void setUp() {
        resourceLoader = new DefaultResourceLoader();
        promptManager = new PromptManager(resourceLoader);
    }

    @Test
    @DisplayName("testLoadPrompt_existingVersion: loads from classpath")
    void testLoadPrompt_existingVersion() {
        String prompt = promptManager.loadPrompt("v1");

        assertThat(prompt).isNotNull();
        assertThat(prompt).isNotEmpty();
        assertThat(prompt).contains("TOBi2");
        assertThat(prompt).contains("Vodafone Albania");
    }

    @Test
    @DisplayName("testLoadPrompt_fallbackToV1: non-existent version -> falls back to v1")
    void testLoadPrompt_fallbackToV1() {
        String nonExistentPrompt = promptManager.loadPrompt("v999-nonexistent");

        assertThat(nonExistentPrompt).isNotNull();
        assertThat(nonExistentPrompt).isNotEmpty();
    }

    @Test
    @DisplayName("testReloadPrompt: evicts cache entry")
    void testReloadPrompt() {
        String firstLoad = promptManager.loadPrompt("v1");
        assertThat(firstLoad).isNotNull();

        promptManager.reloadPrompt("v1");

        String secondLoad = promptManager.loadPrompt("v1");
        assertThat(secondLoad).isNotNull();
        assertThat(secondLoad).isEqualTo(firstLoad);
    }
}
