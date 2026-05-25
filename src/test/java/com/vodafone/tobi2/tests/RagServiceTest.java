package com.vodafone.tobi2.tests;

import com.vodafone.tobi2.service.RagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RagService Tests")
class RagServiceTest {

    private RagService ragService;

    @BeforeEach
    void setUp() {
        ragService = new RagService();
    }

    @Test
    @DisplayName("testSearch_exactMatch: known query -> returns matching entry")
    void testSearch_exactMatch() {
        List<String> results = ragService.search("unlimited-max");
        assertThat(results).isNotEmpty();
        assertThat(results.get(0)).contains("Unlimited Max");
        assertThat(results.get(0)).contains("1500 Lek");
    }

    @Test
    @DisplayName("testSearch_partialMatch: substring -> returns matching entries")
    void testSearch_partialMatch() {
        List<String> results = ragService.search("roaming");
        assertThat(results).isNotEmpty();
        assertThat(results).anyMatch(r -> r.contains("Roaming") && r.contains("EU"));
    }

    @Test
    @DisplayName("testSearch_noMatch: gibberish -> empty list")
    void testSearch_noMatch() {
        List<String> results = ragService.search("xyz123nonexistent999");
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("testSearch_caseInsensitive: different case -> still matches")
    void testSearch_caseInsensitive() {
        List<String> results1 = ragService.search("UNLIMITED-MAX");
        List<String> results2 = ragService.search("Unlimited-Max");
        List<String> results3 = ragService.search("unlimited-max");

        assertThat(results1).isNotEmpty();
        assertThat(results2).isNotEmpty();
        assertThat(results3).isNotEmpty();
        assertThat(results1.get(0)).isEqualTo(results2.get(0));
        assertThat(results2.get(0)).isEqualTo(results3.get(0));
    }
}
