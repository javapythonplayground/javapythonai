package com.codemind.controller;

import com.codemind.model.CodeRequest;
import com.codemind.model.CodeResponse;
import com.codemind.model.Notification;
import com.codemind.service.AnthropicService;
import com.codemind.service.NotificationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * CodeController — exposes all CodeMind REST endpoints.
 *
 * Endpoints:
 *   POST /api/generate        Generate Java or Python code from a prompt
 *   POST /api/debug           Debug existing Java or Python code
 *   POST /api/process         Unified endpoint (mode determined by request body)
 *   GET  /api/stream          SSE endpoint — streams notifications in real time
 *   GET  /api/health          Health check
 *
 * All endpoints return JSON. The /api/stream endpoint returns
 * text/event-stream for real-time action-log notifications.
 */
@RestController
@RequestMapping("/api")
public class CodeController {

    private static final Logger log = LoggerFactory.getLogger(CodeController.class);

    private final AnthropicService    anthropicService;
    private final NotificationService notificationService;

    public CodeController(AnthropicService anthropicService,
                          NotificationService notificationService) {
        this.anthropicService    = anthropicService;
        this.notificationService = notificationService;
    }

    // ── Health Check ──────────────────────────────────────────────────────────

    /**
     * GET /api/health
     * Returns a simple status payload to confirm the service is running.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status",  "UP",
                "service", "CodeMind LLM",
                "version", "1.0.0"
        ));
    }

    // ── Code Generation ───────────────────────────────────────────────────────

    /**
     * POST /api/generate
     *
     * Request body:
     * {
     *   "language": "java" | "python",
     *   "prompt":   "describe what to build"
     * }
     *
     * Response: {@link CodeResponse} with outputCode and notifications.
     */
    @PostMapping("/generate")
    public ResponseEntity<CodeResponse> generate(@Valid @RequestBody CodeRequest request) {
        log.info("POST /api/generate | lang={} | prompt={}",
                 request.getLanguage(), truncate(request.getPrompt(), 60));

        request.setMode("generate");
        CodeResponse response = anthropicService.process(request);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.internalServerError().body(response);
    }

    // ── Code Debugging ────────────────────────────────────────────────────────

    /**
     * POST /api/debug
     *
     * Request body:
     * {
     *   "language": "java" | "python",
     *   "code":     "... source code to analyse ...",
     *   "prompt":   "(optional) what this code is supposed to do"
     * }
     *
     * Response: {@link CodeResponse} with issues[], outputCode (fixed), and notifications.
     */
    @PostMapping("/debug")
    public ResponseEntity<CodeResponse> debug(@Valid @RequestBody CodeRequest request) {
        if (request.getCodeToDebug() == null || request.getCodeToDebug().isBlank()) {
            CodeResponse err = CodeResponse.failure(
                    "Field 'code' is required for debug mode.",
                    List.of(Notification.error("No code provided for debugging.")));
            return ResponseEntity.badRequest().body(err);
        }

        log.info("POST /api/debug | lang={} | lines={}",
                 request.getLanguage(),
                 request.getCodeToDebug().split("\n").length);

        request.setMode("debug");
        CodeResponse response = anthropicService.process(request);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.internalServerError().body(response);
    }

    // ── Unified Process Endpoint ──────────────────────────────────────────────

    /**
     * POST /api/process
     *
     * Unified endpoint — mode is determined by the "mode" field in the request body.
     * Accepts either "generate" or "debug" in the same endpoint.
     */
    @PostMapping("/process")
    public ResponseEntity<CodeResponse> process(@Valid @RequestBody CodeRequest request) {
        log.info("POST /api/process | lang={} | mode={}", request.getLanguage(), request.getMode());

        if (request.isDebugMode() &&
                (request.getCodeToDebug() == null || request.getCodeToDebug().isBlank())) {
            CodeResponse err = CodeResponse.failure(
                    "Field 'code' is required when mode is 'debug'.",
                    List.of(Notification.error("No code provided for debugging.")));
            return ResponseEntity.badRequest().body(err);
        }

        CodeResponse response = anthropicService.process(request);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.internalServerError().body(response);
    }

    // ── SSE Notification Stream ───────────────────────────────────────────────

    /**
     * GET /api/stream?lang={language}&mode={mode}&prompt={prompt}
     *
     * Server-Sent Events endpoint that streams action-log {@link Notification}
     * entries in real time as the LLM processes a request.
     *
     * NOTE: For simplicity this streams a fixed sequence of notifications
     * representative of the processing steps. For full bidirectional streaming
     * of the LLM token output, integrate the Anthropic streaming API
     * (anthropic-version >= 2023-06-01 with stream=true).
     *
     * Clients connect with EventSource:
     *   const es = new EventSource('/api/stream?lang=java&mode=generate&prompt=...');
     *   es.onmessage = e => console.log(JSON.parse(e.data));
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Notification> streamNotifications(
            @RequestParam(defaultValue = "python") String lang,
            @RequestParam(defaultValue = "generate") String mode,
            @RequestParam(defaultValue = "") String prompt) {

        log.info("GET /api/stream | lang={} | mode={} | prompt={}", lang, mode, truncate(prompt, 40));

        List<Notification> notifications = "debug".equalsIgnoreCase(mode)
                ? notificationService.buildDebugStartNotifications(lang, 0)
                : notificationService.buildGenerateStartNotifications(lang, prompt);

        // Emit each notification with a 400 ms delay for a real-time feel
        return Flux.fromIterable(notifications)
                   .delayElements(Duration.ofMillis(400));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
