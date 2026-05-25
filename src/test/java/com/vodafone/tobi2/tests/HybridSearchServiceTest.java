package com.vodafone.tobi2.tests;

import com.vodafone.tobi2.rag.HybridSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HybridSearchService Tests")
class HybridSearchServiceTest {

    private HybridSearchService hybridSearchService;

    @BeforeEach
    void setUp() {
        hybridSearchService = new HybridSearchService();
    }

    @Test
    @DisplayName("testIndexAndSearch: index chunks -> search returns results")
    void testIndexAndSearch() {
        List<String> chunks = List.of(
                "Vodafone Albania offers Unlimited Max plan with 5G data",
                "Smart S is a budget plan with 5GB data for 400 Lek"
        );

        hybridSearchService.indexChunks(chunks, "test-source");

        List<HybridSearchService.SearchResult> results = hybridSearchService.search("Unlimited Max plan", 5);

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).text()).contains("Unlimited Max");
    }

    @Test
    @DisplayName("testSearch_topK: index 5 chunks -> search topK(2) returns 2 results")
    void testSearch_topK() {
        List<String> chunks = List.of(
                "First chunk about data plans",
                "Second chunk about roaming",
                "Third chunk about billing",
                "Fourth chunk about 5G",
                "Fifth chunk about customer service"
        );

        hybridSearchService.indexChunks(chunks, "test-plans");

        List<HybridSearchService.SearchResult> results = hybridSearchService.search("chunk", 2);

        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("testSearch_scoringOrder: results sorted by score descending")
    void testSearch_scoringOrder() {
        List<String> chunks = List.of(
                "data data data data vodafone plan",
                "random unrelated text without keywords",
                "data vodafone plan something else"
        );

        hybridSearchService.indexChunks(chunks, "test-scoring");

        List<HybridSearchService.SearchResult> results = hybridSearchService.search("data vodafone plan", 5);

        assertThat(results).hasSizeGreaterThanOrEqualTo(2);
        assertThat(results.get(0).score()).isGreaterThanOrEqualTo(results.get(1).score());
    }
}
