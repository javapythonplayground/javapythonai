package com.codemind.service;

import com.codemind.model.CodeResponse.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * NotificationService
 *
 * Tracks every action performed during a code generation or debug session
 * and exposes them as a list of typed notification events to send to the frontend.
 *
 * Each event has a type (info | success | warning | error | processing)
 * and a human-readable message describing exactly what the system is doing.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    /**
     * Creates a fresh list and adds an initial "session started" notification.
     */
    public List<NotificationEvent> startSession(String mode, String language) {
        List<NotificationEvent> events = new ArrayList<>();
        notify(events, "info", "CodeMind session started — mode: " + mode + ", language: " + language.toUpperCase());
        return events;
    }

    public void notifyPromptReceived(List<NotificationEvent> events, String prompt) {
        String preview = prompt.length() > 60 ? prompt.substring(0, 60) + "..." : prompt;
        notify(events, "info", "Prompt received: \"" + preview + "\"");
    }

    public void notifyCodeReceived(List<NotificationEvent> events, int lineCount) {
        notify(events, "info", "Source code received — " + lineCount + " lines to analyse.");
    }

    public void notifyBuildingPrompt(List<NotificationEvent> events, String language) {
        notify(events, "processing", "Building " + language.toUpperCase() + " system prompt for LLM...");
    }

    public void notifySendingRequest(List<NotificationEvent> events, String model) {
        notify(events, "processing", "Sending request to Anthropic API (model: " + model + ")...");
    }

    public void notifyResponseReceived(List<NotificationEvent> events) {
        notify(events, "success", "LLM response received successfully.");
    }

    public void notifyParsingOutput(List<NotificationEvent> events) {
        notify(events, "processing", "Parsing and formatting LLM output...");
    }

    public void notifyGenerationComplete(List<NotificationEvent> events, int lineCount, String language) {
        notify(events, "success", "Code generation complete — " + lineCount + " lines of " + language.toUpperCase() + " produced.");
    }

    public void notifyDebugScanStart(List<NotificationEvent> events) {
        notify(events, "processing", "Scanning for syntax errors, logic bugs, and anti-patterns...");
    }

    public void notifyDebugIssuesFound(List<NotificationEvent> events, int errors, int warnings, int infos) {
        if (errors > 0) {
            notify(events, "error", errors + " error(s) detected in your code.");
        }
        if (warnings > 0) {
            notify(events, "warning", warnings + " warning(s) found.");
        }
        if (infos > 0) {
            notify(events, "info", infos + " informational suggestion(s) noted.");
        }
        if (errors == 0 && warnings == 0) {
            notify(events, "success", "No critical issues found — code looks clean!");
        }
    }

    public void notifyFixedCodeReady(List<NotificationEvent> events) {
        notify(events, "success", "Fixed/corrected code section generated.");
    }

    public void notifyError(List<NotificationEvent> events, String detail) {
        notify(events, "error", "Error encountered: " + detail);
    }

    public void notifyApiKeyMissing(List<NotificationEvent> events) {
        notify(events, "error", "ANTHROPIC_API_KEY is not configured. Set it in application.properties or as an env variable.");
    }

    // ---- private helpers ----

    private void notify(List<NotificationEvent> events, String type, String message) {
        events.add(new NotificationEvent(type, message));
        log.info("[{}] {}", type.toUpperCase(), message);
    }
}
