package com.codemind.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * POJO that maps to the Anthropic /v1/messages request body.
 */
public class AnthropicApiRequest {

    private String model;

    @JsonProperty("max_tokens")
    private int maxTokens;

    private String system;

    private List<Map<String, String>> messages;

    public AnthropicApiRequest(String model, int maxTokens, String system, List<Map<String, String>> messages) {
        this.model = model;
        this.maxTokens = maxTokens;
        this.system = system;
        this.messages = messages;
    }

    public String getModel()  { return model; }
    public int getMaxTokens() { return maxTokens; }
    public String getSystem() { return system; }
    public List<Map<String, String>> getMessages() { return messages; }
}
