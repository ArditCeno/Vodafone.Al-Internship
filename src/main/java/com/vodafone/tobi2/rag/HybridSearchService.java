package com.vodafone.tobi2.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class HybridSearchService {

    private static final Logger log = LoggerFactory.getLogger(HybridSearchService.class);

    private final List<DocumentChunk> documentStore = new ArrayList<>();
    private long chunkIdCounter = 0;

    public void indexChunks(List<String> chunks, String source) {
        for (String chunk : chunks) {
            documentStore.add(new DocumentChunk(++chunkIdCounter, chunk, source, extractKeywords(chunk)));
        }
        log.info("Indexed {} chunks from {}", chunks.size(), source);
    }

    public List<SearchResult> search(String query, int topK) {
        if (documentStore.isEmpty()) return List.of();
        String q = query.toLowerCase();
        Set<String> queryKeywords = extractKeywords(q);

        List<ScoredResult> scored = new ArrayList<>();
        for (DocumentChunk doc : documentStore) {
            double vectorScore = computeSimpleVectorScore(q, doc.text);
            double keywordScore = computeKeywordScore(queryKeywords, doc.keywords);
            double hybridScore = 0.6 * vectorScore + 0.4 * keywordScore;
            scored.add(new ScoredResult(doc.id, doc.text, doc.source, hybridScore));
        }

        scored.sort((a, b) -> Double.compare(b.score, a.score));
        return scored.stream()
                .limit(topK)
                .map(s -> new SearchResult(s.text, s.source, s.score))
                .collect(Collectors.toList());
    }

    private double computeSimpleVectorScore(String query, String text) {
        String t = text.toLowerCase();
        long matchCount = Arrays.stream(query.split("\\s+"))
                .filter(word -> word.length() > 2)
                .filter(t::contains)
                .count();
        long wordCount = Arrays.stream(query.split("\\s+")).filter(w -> w.length() > 2).count();
        if (wordCount == 0) return 0;
        return (double) matchCount / wordCount;
    }

    private double computeKeywordScore(Set<String> queryKeywords, Set<String> docKeywords) {
        if (queryKeywords.isEmpty() || docKeywords.isEmpty()) return 0;
        long intersection = queryKeywords.stream().filter(docKeywords::contains).count();
        return (double) intersection / Math.max(queryKeywords.size(), docKeywords.size());
    }

    private Set<String> extractKeywords(String text) {
        return Arrays.stream(text.toLowerCase().split("[^a-zA-Z0-9]+"))
                .filter(w -> w.length() > 2)
                .collect(Collectors.toSet());
    }

    private record DocumentChunk(long id, String text, String source, Set<String> keywords) {}
    public record ScoredResult(long id, String text, String source, double score) {}
    public record SearchResult(String text, String source, double score) {}
}
