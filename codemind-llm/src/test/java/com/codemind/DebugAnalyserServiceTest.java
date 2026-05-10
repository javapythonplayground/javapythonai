package com.codemind;

import com.codemind.model.CodeResponse.DebugSummary;
import com.codemind.service.DebugAnalyserService;
import com.codemind.service.NotificationService;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

class DebugAnalyserServiceTest {

    private final NotificationService  ns  = new NotificationService();
    private final DebugAnalyserService das = new DebugAnalyserService(ns);

    @Test
    void testCountsErrors() {
        String report = "// === CODEMIND DEBUG REPORT ===\n" +
                        "[ERROR] Line 5: NullPointerException — Fix: add null check\n" +
                        "[ERROR] Line 10: ArrayIndexOutOfBounds — Fix: check length\n" +
                        "[WARNING] Line 15: unused import\n" +
                        "// === FIXED CODE ===\npublic class Fixed {}\n" +
                        "// === SUMMARY ===\nTwo critical errors fixed.";

        DebugSummary summary = das.analyse(report, new ArrayList<>());
        assertEquals(2, summary.getErrors());
        assertEquals(1, summary.getWarnings());
        assertEquals(0, summary.getInfos());
    }

    @Test
    void testExtractsFixedCode() {
        String report = "// === CODEMIND DEBUG REPORT ===\nNo issues.\n" +
                        "// === FIXED CODE ===\npublic class Clean { }\n" +
                        "// === SUMMARY ===\nCode is clean.";

        DebugSummary summary = das.analyse(report, new ArrayList<>());
        assertTrue(summary.getFixedCode().contains("Clean"));
    }

    @Test
    void testNoIssuesReport() {
        String report = "// === CODEMIND DEBUG REPORT ===\n" +
                        "No issues detected. Code is correct.\n" +
                        "// === FIXED CODE ===\n# No changes needed.\n" +
                        "// === SUMMARY ===\nCode quality is high.";

        DebugSummary summary = das.analyse(report, new ArrayList<>());
        assertEquals(0, summary.getErrors());
        assertEquals(0, summary.getWarnings());
    }
}
