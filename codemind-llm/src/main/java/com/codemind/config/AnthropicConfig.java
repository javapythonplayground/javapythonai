package com.codemind.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration for Anthropic Claude API client.
 *
 * Set your API key via:
 *   Environment variable: ANTHROPIC_API_KEY=sk-ant-...
 *   Or in application.properties: anthropic.api.key=sk-ant-...
 */
@Configuration
public class AnthropicConfig {

    @Value("${anthropic.api.key:#{environment['ANTHROPIC_API_KEY']}}")
    private String apiKey;

    @Value("${anthropic.api.url:https://api.anthropic.com}")
    private String apiUrl;

    @Value("${anthropic.api.version:2023-06-01}")
    private String apiVersion;

    @Value("${anthropic.model:claude-sonnet-4-20250514}")
    private String model;

    @Value("${anthropic.max.tokens:2048}")
    private int maxTokens;

    @Bean
    public WebClient anthropicWebClient() {
        return WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("x-api-key", apiKey != null ? apiKey : "")
                .defaultHeader("anthropic-version", apiVersion)
                .defaultHeader("Content-Type", "application/json")
                .codecs(config -> config.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    public String getModel() { return model; }
    public int getMaxTokens() { return maxTokens; }
    public String getApiKey() { return apiKey; }
}
