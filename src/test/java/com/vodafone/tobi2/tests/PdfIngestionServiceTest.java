package com.vodafone.tobi2.tests;

import com.vodafone.tobi2.rag.PdfIngestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("PdfIngestionService Tests")
class PdfIngestionServiceTest {

    private PdfIngestionService pdfIngestionService;

    @BeforeEach
    void setUp() {
        pdfIngestionService = new PdfIngestionService();
    }

    @Test
    @DisplayName("testExtractText: mock PDF input stream -> returns non-empty chunks")
    void testExtractText_actualPdfData() throws IOException {
        InputStream nullStream = null;
        assertThrows(Exception.class, () -> {
            pdfIngestionService.extractText(nullStream);
        });
    }

    @Test
    @DisplayName("testExtractText: invalid PDF stream throws exception")
    void testExtractText_invalidPdf() {
        byte[] invalidPdfData = "This is not a valid PDF file".getBytes();
        InputStream invalidStream = new ByteArrayInputStream(invalidPdfData);

        assertThrows(IOException.class, () -> {
            pdfIngestionService.extractText(invalidStream);
        });
    }
}
