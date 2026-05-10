package com.codemind.service;

import com.codemind.model.CodeRequest;
import com.codemind.model.CodeResponse;
import com.codemind.model.CodeResponse.NotificationEvent;
import com.codemind.model.CodeResponse.DebugSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * CodeMindService — Core Orchestration Layer
 *
 * Coordinates the full lifecycle of a code generation or debug request:
 *   1. Start a notification session
 *   2. Validate input
 *   3. Build the appropriate LLM prompt
 *   4. Call Anthropic Claude API
 *   5. Parse and enrich the response
 *   6. Return a fully-populated CodeResponse with all notification events
 */
@Service
public class CodeMindService {

    private static final Logger log = LoggerFactory.getLogger(CodeMindService.class);

    private final NotificationService   notificationService;
    private final PromptBuilderService  promptBuilderService;
    private final AnthropicClientService anthropicClientService;
    private final DebugAnalyserService  debugAnalyserService;

    public CodeMindService(NotificationService notificationService,
                           PromptBuilderService promptBuilderService,
                           AnthropicClientService anthropicClientService,
                           DebugAnalyserService debugAnalyserService) {
        this.notificationService   = notificationService;
        this.promptBuilderService  = promptBuilderService;
        this.anthropicClientService = anthropicClientService;
        this.debugAnalyserService  = debugAnalyserService;
    }

    // ------------------------------------------------------------------ //
    //  PUBLIC ENTRY POINT
    // ------------------------------------------------------------------ //

    public CodeResponse process(CodeRequest request) {

        List<NotificationEvent> events =
                notificationService.startSession(request.getMode(), request.getLanguage());

        try {
            return "generate".equalsIgnoreCase(request.getMode())
                    ? handleGenerate(request, events)
                    : handleDebug(request, events);

        } catch (Exception ex) {
            log.error("CodeMind processing error", ex);
            notificationService.notifyError(events, ex.getMessage());
            return CodeResponse.error(ex.getMessage(), events);
        }
    }

    // ------------------------------------------------------------------ //
    //  GENERATE MODE
    // ------------------------------------------------------------------ //

    private CodeResponse handleGenerate(CodeRequest req, List<NotificationEvent> events) {

        // 1. Validate prompt
        if (req.getPrompt() == null || req.getPrompt().isBlank()) {
            notificationService.notifyError(events, "Prompt cannot be empty in generate mode.");
            return CodeResponse.error("Prompt is required for code generation.", events);
        }
        notificationService.notifyPromptReceived(events, req.getPrompt());

        // 2. Build prompts
        notificationService.notifyBuildingPrompt(events, req.getLanguage());
        String systemPrompt = promptBuilderService.buildSystemPrompt("generate", req.getLanguage());
        String userMessage  = promptBuilderService.buildGenerateUserMessage(req.getLanguage(), req.getPrompt());

        // 3. Call Claude
        String rawOutput = anthropicClientService.callClaude(systemPrompt, userMessage, events);

        // 4. Parse output
        notificationService.notifyParsingOutput(events);
        int lineCount = rawOutput.split("\n").length;
        notificationService.notifyGenerationComplete(events, lineCount, req.getLanguage());

        // 5. Build response
        CodeResponse response = new CodeResponse();
        response.setSuccess(true);
        response.setOutput(rawOutput);
        response.setLanguage(req.getLanguage());
        response.setMode("generate");
        response.setLineCount(lineCount);
        response.setNotifications(events);
        return response;
    }

    // ------------------------------------------------------------------ //
    //  DEBUG MODE
    // ------------------------------------------------------------------ //

    private CodeResponse handleDebug(CodeRequest req, List<NotificationEvent> events) {

        // 1. Validate code present
        if (req.getCode() == null || req.getCode().isBlank()) {
            notificationService.notifyError(events, "No code provided for debug mode.");
            return CodeResponse.error("Code is required for debug mode.", events);
        }

        int lineCount = req.getCode().split("\n").length;
        notificationService.notifyCodeReceived(events, lineCount);

        if (req.getPrompt() != null && !req.getPrompt().isBlank()) {
            notificationService.notifyPromptReceived(events, req.getPrompt());
        }

        // 2. Build prompts
        notificationService.notifyBuildingPrompt(events, req.getLanguage());
        String systemPrompt = promptBuilderService.buildSystemPrompt("debug", req.getLanguage());
        String userMessage  = promptBuilderService.buildDebugUserMessage(
                req.getLanguage(), req.getCode(), req.getPrompt());

        // 3. Call Claude
        String rawOutput = anthropicClientService.callClaude(systemPrompt, userMessage, events);

        // 4. Analyse debug report
        notificationService.notifyParsingOutput(events);
        DebugSummary summary = debugAnalyserService.analyse(rawOutput, events);

        // 5. Build response
        CodeResponse response = new CodeResponse();
        response.setSuccess(true);
        response.setOutput(rawOutput);
        response.setLanguage(req.getLanguage());
        response.setMode("debug");
        response.setLineCount(rawOutput.split("\n").length);
        response.setDebugSummary(summary);
        response.setNotifications(events);
        return response;
    }
}
