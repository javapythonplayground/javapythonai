package com.codemind.service;

import com.codemind.model.CodeResponse.DebugSummary;
import com.codemind.model.CodeResponse.NotificationEvent;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DebugAnalyserService
 *
 * Parses the raw debug report text returned by Claude,
 * counts issue severities, extracts the fixed code block,
 * and fires granular notification events.
 */
@Service
public class DebugAnalyserService {

    private static final Pattern ERROR_PATTERN   = Pattern.compile("\\[ERROR\\]",   Pattern.CASE_INSENSITIVE);
    private static final Pattern WARNING_PATTERN = Pattern.compile("\\[WARNING\\]", Pattern.CASE_INSENSITIVE);
    private static final Pattern INFO_PATTERN    = Pattern.compile("\\[INFO\\]",    Pattern.CASE_INSENSITIVE);

    private final NotificationService notificationService;

    public DebugAnalyserService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Analyses the Claude debug report, fires notifications, returns a DebugSummary.
     */
    public DebugSummary analyse(String rawReport, List<NotificationEvent> events) {

        notificationService.notifyDebugScanStart(events);

        int errors   = countMatches(ERROR_PATTERN,   rawReport);
        int warnings = countMatches(WARNING_PATTERN, rawReport);
        int infos    = countMatches(INFO_PATTERN,    rawReport);

        notificationService.notifyDebugIssuesFound(events, errors, warnings, infos);

        String fixedCode = extractSection(rawReport, "FIXED CODE", "SUMMARY");
        if (fixedCode != null && !fixedCode.isBlank()) {
            notificationService.notifyFixedCodeReady(events);
        }

        return new DebugSummary(errors, warnings, infos, fixedCode != null ? fixedCode.trim() : "");
    }

    // ---- helpers ----

    private int countMatches(Pattern pattern, String text) {
        Matcher m = pattern.matcher(text);
        int count = 0;
        while (m.find()) count++;
        return count;
    }

    /**
     * Extracts text between two section markers (e.g. "FIXED CODE" ... "SUMMARY").
     */
    private String extractSection(String text, String startMarker, String endMarker) {
        int start = text.indexOf(startMarker);
        if (start == -1) return null;
        start = text.indexOf('\n', start) + 1;          // skip the marker line
        int end = text.indexOf(endMarker, start);
        if (end == -1) end = text.length();
        return text.substring(start, end);
    }
}
