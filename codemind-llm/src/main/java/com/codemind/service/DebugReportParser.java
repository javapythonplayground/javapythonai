package com.codemind.service;

import com.codemind.model.DebugIssue;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DebugReportParser — parses the structured text output from the LLM debug
 * prompt into a list of {@link DebugIssue} objects.
 *
 * Expected input format (from LLM):
 *   // === CODEMIND DEBUG REPORT ===
 *   [ERROR] Line 12: NullPointerException possible — Fix: add null check before calling .size()
 *   [WARNING] Line 7: Magic number — Fix: extract to a named constant
 *   // === FIXED CODE ===
 *   ... fixed source code ...
 *   // === SUMMARY ===
 *   ... one-line summary ...
 */
@Component
public class DebugReportParser {

    // Matches lines like: [ERROR] Line 12: description — Fix: suggestion
    // Also handles lines without "Line N:" e.g. [INFO] No issues found
    private static final Pattern ISSUE_PATTERN = Pattern.compile(
            "^\\[(ERROR|WARNING|INFO)]\\s+(?:Line\\s+(\\d+):\\s+)?(.+?)(?:\\s+[-—]+\\s+Fix:\\s+(.+))?$",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Extracts structured {@link DebugIssue} objects from raw LLM response text.
     *
     * @param rawResponse the full text response from the LLM
     * @return list of parsed issues (may be empty if none found or parse fails)
     */
    public List<DebugIssue> parse(String rawResponse) {
        List<DebugIssue> issues = new ArrayList<>();
        if (rawResponse == null || rawResponse.isBlank()) return issues;

        // Isolate the debug report section
        String reportSection = extractSection(rawResponse, "CODEMIND DEBUG REPORT", "FIXED CODE");
        if (reportSection == null) {
            reportSection = rawResponse; // Fall back to scanning entire response
        }

        for (String line : reportSection.split("\n")) {
            line = line.strip();
            if (line.isEmpty() || line.startsWith("//")) continue;

            Matcher m = ISSUE_PATTERN.matcher(line);
            if (m.matches()) {
                DebugIssue issue = new DebugIssue();
                issue.setSeverity(parseSeverity(m.group(1)));
                issue.setLineNumber(m.group(2) != null ? Integer.parseInt(m.group(2)) : -1);
                issue.setDescription(m.group(3) != null ? m.group(3).strip() : "");
                issue.setSuggestion(m.group(4) != null ? m.group(4).strip() : "");
                issue.setRawText(line);
                issues.add(issue);
            }
        }

        return issues;
    }

    /**
     * Extracts the fixed code section from the LLM response.
     *
     * @param rawResponse full LLM response
     * @return the corrected source code, or null if section not found
     */
    public String extractFixedCode(String rawResponse) {
        return extractSection(rawResponse, "FIXED CODE", "SUMMARY");
    }

    /**
     * Extracts the one-line summary from the LLM response.
     *
     * @param rawResponse full LLM response
     * @return the summary string, or null if not found
     */
    public String extractSummary(String rawResponse) {
        String section = extractSection(rawResponse, "SUMMARY", null);
        if (section == null) return null;
        return section.lines()
                      .map(String::strip)
                      .filter(l -> !l.isEmpty())
                      .findFirst()
                      .orElse(null);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Extracts text between two section headers (exclusive).
     * Headers are matched as substrings of "// === HEADER ===" lines.
     */
    private String extractSection(String text, String startMarker, String endMarker) {
        int startIdx = findMarker(text, startMarker);
        if (startIdx < 0) return null;

        // Skip past the marker line itself
        int afterStart = text.indexOf('\n', startIdx);
        if (afterStart < 0) return null;
        afterStart++; // skip the newline

        if (endMarker == null) {
            return text.substring(afterStart).strip();
        }

        int endIdx = findMarker(text, endMarker);
        if (endIdx < 0 || endIdx <= afterStart) {
            return text.substring(afterStart).strip();
        }

        return text.substring(afterStart, endIdx).strip();
    }

    private int findMarker(String text, String marker) {
        String upperText = text.toUpperCase();
        String upperMarker = marker.toUpperCase();
        int idx = upperText.indexOf(upperMarker);
        if (idx < 0) return -1;
        // Walk back to start of the line
        int lineStart = text.lastIndexOf('\n', idx);
        return lineStart < 0 ? 0 : lineStart;
    }

    private DebugIssue.Severity parseSeverity(String s) {
        return switch (s.toUpperCase()) {
            case "ERROR"   -> DebugIssue.Severity.ERROR;
            case "WARNING" -> DebugIssue.Severity.WARNING;
            default        -> DebugIssue.Severity.INFO;
        };
    }
}
