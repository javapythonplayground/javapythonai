package com.codemind.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * POJO that maps the Anthropic /v1/messages JSON response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnthropicApiResponse {

    private String id;
    private String type;
    private String role;
    private List<ContentBlock> content;
    private String model;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContentBlock {
        private String type;
        private String text;

        public String getType() { return type; }
        public void   setType(String type) { this.type = type; }
        public String getText() { return text; }
        public void   setText(String text) { this.text = text; }
    }

    public String getId()    { return id; }
    public void   setId(String id) { this.id = id; }

    public String getType()  { return type; }
    public void   setType(String type) { this.type = type; }

    public String getRole()  { return role; }
    public void   setRole(String role) { this.role = role; }

    public List<ContentBlock> getContent() { return content; }
    public void setContent(List<ContentBlock> content) { this.content = content; }

    public String getModel() { return model; }
    public void   setModel(String model) { this.model = model; }

    /**
     * Convenience: extract all text blocks joined together.
     */
    public String extractText() {
        if (content == null) return "";
        return content.stream()
                .filter(b -> "text".equals(b.getType()))
                .map(ContentBlock::getText)
                .reduce("", String::concat);
    }
}
