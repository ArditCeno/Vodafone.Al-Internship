package com.vodafone.tobi2.rag;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class PdfIngestionService {

    private static final Logger log = LoggerFactory.getLogger(PdfIngestionService.class);

    public List<String> extractText(InputStream pdfStream) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfStream.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String fullText = stripper.getText(document);
            return chunkText(fullText);
        }
    }

    public List<String> extractTextFromFile(File pdfFile) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String fullText = stripper.getText(document);
            return chunkText(fullText);
        }
    }

    private List<String> chunkText(String text) {
        List<String> chunks = new ArrayList<>();
        String[] paragraphs = text.split("\n\n+");
        StringBuilder current = new StringBuilder();
        for (String p : paragraphs) {
            String trimmed = p.trim();
            if (trimmed.isBlank()) continue;
            if (current.length() + trimmed.length() > 1000) {
                chunks.add(current.toString().trim());
                current = new StringBuilder();
            }
            current.append(trimmed).append("\n\n");
        }
        if (!current.isEmpty()) {
            chunks.add(current.toString().trim());
        }
        return chunks;
    }
}
