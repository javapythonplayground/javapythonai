package com.codemind.controller;

import com.codemind.model.CodeRequest;
import com.codemind.model.CodeResponse;
import com.codemind.service.CodeMindService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * CodeMindController — REST API Layer
 *
 * Endpoints:
 *   POST /api/code/generate  — Generate Java or Python code from a natural-language prompt
 *   POST /api/code/debug     — Debug and fix submitted source code
 *   GET  /api/health         — Health check
 */
@RestController
@RequestMapping("/api")
public class CodeMindController {

    private static final Logger log = LoggerFactory.getLogger(CodeMindController.class);

    private final CodeMindService codeMindService;

    public CodeMindController(CodeMindService codeMindService) {
        this.codeMindService = codeMindService;
    }

    // ------------------------------------------------------------------ //
    //  Health check
    // ------------------------------------------------------------------ //

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status",  "UP",
                "service", "CodeMind LLM",
                "version", "1.0.0"
        ));
    }

    // ------------------------------------------------------------------ //
    //  Code Generation
    // ------------------------------------------------------------------ //

    /**
     * Generates Java or Python source code from a natural-language prompt.
     *
     * Request body:
     * {
     *   "mode":     "generate",
     *   "language": "java" | "python",
     *   "prompt":   "Write a binary search tree with insert and traversal"
     * }
     */
    @PostMapping("/code/generate")
    public ResponseEntity<CodeResponse> generateCode(@Valid @RequestBody CodeRequest request) {
        log.info("Generate request received — language: {}, prompt: {}",
                request.getLanguage(),
                request.getPrompt() != null
                        ? request.getPrompt().substring(0, Math.min(60, request.getPrompt().length()))
                        : "null");

        request.setMode("generate");
        CodeResponse response = codeMindService.process(request);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    // ------------------------------------------------------------------ //
    //  Code Debugging
    // ------------------------------------------------------------------ //

    /**
     * Debugs submitted source code and returns a structured report + fixed code.
     *
     * Request body:
     * {
     *   "mode":     "debug",
     *   "language": "java" | "python",
     *   "prompt":   "Optional context about what the code should do",
     *   "code":     "...your source code here..."
     * }
     */
    @PostMapping("/code/debug")
    public ResponseEntity<CodeResponse> debugCode(@Valid @RequestBody CodeRequest request) {
        log.info("Debug request received — language: {}, lines: {}",
                request.getLanguage(),
                request.getCode() != null ? request.getCode().split("\n").length : 0);

        request.setMode("debug");
        CodeResponse response = codeMindService.process(request);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    // ------------------------------------------------------------------ //
    //  Unified endpoint (supports both modes via body.mode field)
    // ------------------------------------------------------------------ //

    @PostMapping("/code")
    public ResponseEntity<CodeResponse> handleCode(@Valid @RequestBody CodeRequest request) {
        log.info("Unified request — mode: {}, language: {}", request.getMode(), request.getLanguage());

        CodeResponse response = codeMindService.process(request);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }
}
