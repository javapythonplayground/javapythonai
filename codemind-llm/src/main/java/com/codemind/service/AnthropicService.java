package com.codemind.service;

import com.codemind.model.CodeRequest;
import com.codemind.model.CodeResponse;
import com.codemind.model.DebugIssue;
import com.codemind.model.Notification;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * AnthropicService — the core service responsible for:
 *
 *   1. Building the JSON request payload for the Anthropic Messages API.
 *   2. Making the HTTP POST call via the configured WebClient.
 *   3. Extracting the text content from the API response.
 *   4. Orchestrating notifications and debug-report parsing.
 *   5. Returning a fully-populated {@link CodeResponse}.
 *
 * Supports two modes:
 *   - GENERATE: produces new Java/Python code from a natural language prompt.
 *   - DEBUG:    analyses existing code and returns a structured issue report + fixed code.
 */
@Service
public class AnthropicService {

    private static final Logger log = LoggerFactory.getLogger(AnthropicService.class);

    private static final String MESSAGES_PATH = "/v1/messages";

    private final WebClient           webClient;
    private final ObjectMapper        objectMapper;
    private final PromptBuilder       promptBuilder;
    private final NotificationService notificationService;
    private final DebugReportParser   debugReportParser;

    @Value("${anthropic.model:claude-sonnet-4-20250514}")
    private String model;

    @Value("${anthropic.max-tokens:2048}")
    private int maxTokens;

    public AnthropicService(@Qualifier("anthropicWebClient") WebClient webClient,
                            ObjectMapper objectMapper,
                            PromptBuilder promptBuilder,
                            NotificationService notificationService,
                            DebugReportParser debugReportParser) {
        this.webClient           = webClient;
        this.objectMapper        = objectMapper;
        this.promptBuilder       = promptBuilder;
        this.notificationService = notificationService;
        this.debugReportParser   = debugReportParser;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Processes a code generation or debugging request synchronously.
     *
     * @param request validated {@link CodeRequest} from the controller
     * @return a {@link CodeResponse} with output code, notifications, and issues
     */
    public CodeResponse process(CodeRequest request) {
        return request.isDebugMode()
                ? processDebug(request)
                : processGenerate(request);
    }

    // ── Generate Mode ─────────────────────────────────────────────────────────

    private CodeResponse processGenerate(CodeRequest request) {
        List<Notification> notifications = new ArrayList<>();

        // Step 1: pre-call notifications
        notifications.addAll(
                notificationService.buildGenerateStartNotifications(
                        request.getLanguage(), request.getPrompt()));

        // Step 2: build and send API request
        String systemPrompt = promptBuilder.buildGenerateSystemPrompt(request.getLanguage());
        String userMessage  = promptBuilder.buildGenerateUserMessage(
                request.getLanguage(), request.getPrompt());

        log.info("GENERATE | lang={} | prompt={}", request.getLanguage(),
                 truncate(request.getPrompt(), 80));

        String rawResponse;
        try {
            rawResponse = callAnthropicApi(systemPrompt, userMessage);
        } catch (Exception ex) {
            log.error("Anthropic API error during generation", ex);
            notifications.addAll(notificationService.buildErrorNotifications(ex.getMessage()));
            return CodeResponse.failure(ex.getMessage(), notifications);
        }

        // Step 3: post-call notifications
        int lineCount = countLines(rawResponse);
        notifications.addAll(
                notificationService.buildGenerateSuccessNotifications(request.getLanguage(), lineCount));

        log.info("GENERATE complete | lines={}", lineCount);
        return CodeResponse.success(
                request.getMode(), request.getLanguage(), rawResponse, notifications, List.of());
    }

    // ── Debug Mode ────────────────────────────────────────────────────────────

    private CodeResponse processDebug(CodeRequest request) {
        List<Notification> notifications = new ArrayList<>();
        int codeLines = countLines(request.getCodeToDebug());

        // Step 1: pre-call notifications
        notifications.addAll(
                notificationService.buildDebugStartNotifications(request.getLanguage(), codeLines));

        // Step 2: build and send API request
        String systemPrompt = promptBuilder.buildDebugSystemPrompt(request.getLanguage());
        String userMessage  = promptBuilder.buildDebugUserMessage(
                request.getLanguage(), request.getCodeToDebug(), request.getPrompt());

        log.info("DEBUG | lang={} | codeLines={}", request.getLanguage(), codeLines);

        String rawResponse;
        try {
            rawResponse = callAnthropicApi(systemPrompt, userMessage);
        } catch (Exception ex) {
            log.error("Anthropic API error during debug", ex);
            notifications.addAll(notificationService.buildErrorNotifications(ex.getMessage()));
            return CodeResponse.failure(ex.getMessage(), notifications);
        }

        // Step 3: parse debug report
        List<DebugIssue> issues   = debugReportParser.parse(rawResponse);
        String fixedCode          = debugReportParser.extractFixedCode(rawResponse);
        String summary            = debugReportParser.extractSummary(rawResponse);

        // Step 4: post-call notifications
        notifications.addAll(notificationService.buildDebugSuccessNotifications(issues));
        if (summary != null) {
            notifications.add(Notification.info("Summary: " + summary, "summary"));
        }

        // Prefer fixed code as the primary output, fall back to raw response
        String output = (fixedCode != null && !fixedCode.isBlank()) ? fixedCode : rawResponse;

        log.info("DEBUG complete | issues={}", issues.size());

        CodeResponse response = CodeResponse.success(
                request.getMode(), request.getLanguage(), output, notifications, issues);
        response.setRawResponse(rawResponse);
        return response;
    }

    // ── Anthropic API communication ───────────────────────────────────────────

    /**
     * Calls the Anthropic Messages API and returns the text content of the response.
     *
     * @param systemPrompt the system instruction
     * @param userMessage  the user turn content
     * @return the model's text response
     * @throws RuntimeException if the API call fails or returns an error status
     */
    private String callAnthropicApi(String systemPrompt, String userMessage) {
        ObjectNode requestBody = buildRequestBody(systemPrompt, userMessage);

        String responseJson = webClient.post()
                .uri(MESSAGES_PATH)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(
                    status -> status.is4xxClientError() || status.is5xxServerError(),
                    clientResponse -> clientResponse.bodyToMono(String.class)
                            .flatMap(body -> Mono.error(
                                    new RuntimeException("Anthropic API error " +
                                            clientResponse.statusCode().value() + ": " + body))))
                .bodyToMono(String.class)
                .block(); // synchronous — controller runs on a virtual thread

        return extractTextFromResponse(responseJson);
    }

    /**
     * Builds the JSON request body for the Anthropic Messages API.
     *
     * Schema:
     * {
     *   "model": "...",
     *   "max_tokens": 2048,
     *   "system": "...",
     *   "messages": [ { "role": "user", "content": "..." } ]
     * }
     */
    private ObjectNode buildRequestBody(String systemPrompt, String userMessage) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", model);
        body.put("max_tokens", maxTokens);
        body.put("system", systemPrompt);

        ArrayNode messages = body.putArray("messages");
        ObjectNode userMsg = messages.addObject();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);

        return body;
    }

    /**
     * Extracts the concatenated text from all content blocks in the API response.
     *
     * Response schema:
     * {
     *   "content": [ { "type": "text", "text": "..." }, ... ]
     * }
     */
    private String extractTextFromResponse(String responseJson) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode contentArray = root.path("content");

            if (!contentArray.isArray()) {
                throw new RuntimeException("Unexpected response structure: 'content' is not an array");
            }

            StringBuilder sb = new StringBuilder();
            for (JsonNode block : contentArray) {
                if ("text".equals(block.path("type").asText())) {
                    sb.append(block.path("text").asText());
                }
            }

            String result = sb.toString().strip();
            if (result.isEmpty()) {
                throw new RuntimeException("Anthropic API returned empty content");
            }
            return result;

        } catch (Exception ex) {
            throw new RuntimeException("Failed to parse Anthropic response: " + ex.getMessage(), ex);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private int countLines(String text) {
        if (text == null || text.isBlank()) return 0;
        return text.split("\n", -1).length;
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
