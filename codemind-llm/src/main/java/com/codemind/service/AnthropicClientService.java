package com.codemind.service;

import com.codemind.config.AnthropicConfig;
import com.codemind.model.AnthropicApiRequest;
import com.codemind.model.AnthropicApiResponse;
import com.codemind.model.CodeResponse.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

/**
 * AnthropicClientService
 *
 * Handles all HTTP communication with the Anthropic Claude API.
 * Wraps WebClient calls, maps request/response POJOs,
 * and fires notification events at every step.
 */
@Service
public class AnthropicClientService {

    private static final Logger log = LoggerFactory.getLogger(AnthropicClientService.class);

    private final WebClient            webClient;
    private final AnthropicConfig      config;
    private final NotificationService  notificationService;

    public AnthropicClientService(WebClient anthropicWebClient,
                                  AnthropicConfig config,
                                  NotificationService notificationService) {
        this.webClient           = anthropicWebClient;
        this.config              = config;
        this.notificationService = notificationService;
    }

    /**
     * Calls the Anthropic /v1/messages endpoint and returns the raw text response.
     *
     * @param systemPrompt  The system instruction for the model.
     * @param userMessage   The user turn content.
     * @param events        Notification list to append progress events.
     * @return              Model response text.
     */
    public String callClaude(String systemPrompt, String userMessage, List<NotificationEvent> events) {

        // Guard: API key must be present
        if (config.getApiKey() == null || config.getApiKey().isBlank()) {
            notificationService.notifyApiKeyMissing(events);
            throw new IllegalStateException("ANTHROPIC_API_KEY is not configured.");
        }

        notificationService.notifySendingRequest(events, config.getModel());

        AnthropicApiRequest requestBody = new AnthropicApiRequest(
                config.getModel(),
                config.getMaxTokens(),
                systemPrompt,
                List.of(Map.of("role", "user", "content", userMessage))
        );

        try {
            AnthropicApiResponse response = webClient.post()
                    .uri("/v1/messages")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(AnthropicApiResponse.class)
                    .block();

            if (response == null || response.getContent() == null || response.getContent().isEmpty()) {
                notificationService.notifyError(events, "Empty response from Anthropic API.");
                throw new RuntimeException("Empty response from Anthropic API.");
            }

            notificationService.notifyResponseReceived(events);
            return response.extractText();

        } catch (WebClientResponseException ex) {
            String detail = "HTTP " + ex.getStatusCode() + " — " + ex.getResponseBodyAsString();
            log.error("Anthropic API error: {}", detail);
            notificationService.notifyError(events, detail);
            throw new RuntimeException("Anthropic API call failed: " + detail, ex);
        }
    }
}
