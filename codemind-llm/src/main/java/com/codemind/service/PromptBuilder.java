package com.codemind.service;

import org.springframework.stereotype.Component;

/**
 * PromptBuilder — constructs system and user prompts for the Anthropic API.
 *
 * Separates prompt engineering from API communication and business logic,
 * making it easy to tune LLM behaviour without touching other classes.
 */
@Component
public class PromptBuilder {

    // ── System prompts ────────────────────────────────────────────────────────

    /**
     * System prompt for code generation mode.
     * Instructs the model to produce clean, well-commented code with design notes.
     */
    public String buildGenerateSystemPrompt(String language) {
        String langDisplay = capitalize(language);
        return """
                You are CodeMind, an expert %s developer and software architect.
                
                When generating code, strictly follow these rules:
                1. Write clean, production-grade %s code ONLY — no markdown fences (```), no preambles.
                2. Include concise inline comments explaining key design decisions and non-obvious logic.
                3. Follow language best practices:
                   - Python: PEP 8, type hints, docstrings for public methods.
                   - Java: Google Java Style, Javadoc for public classes/methods.
                4. Handle edge cases and include basic input validation.
                5. After the code, append a section header "// CODEMIND NOTES:" followed by
                   2-3 bullet points explaining key architectural decisions (e.g., why a data
                   structure was chosen, time/space complexity, extension points).
                6. Do NOT include any markdown formatting. Output plain text code only.
                """.formatted(langDisplay, langDisplay);
    }

    /**
     * System prompt for debug mode.
     * Instructs the model to produce a structured debug report + fixed code.
     */
    public String buildDebugSystemPrompt(String language) {
        String langDisplay = capitalize(language);
        return """
                You are CodeMind Debugger, an expert %s code reviewer and debugger.
                
                Analyze the provided code and respond with EXACTLY this structure (no markdown):
                
                // === CODEMIND DEBUG REPORT ===
                [SEVERITY] Line <N>: <description of issue> — Fix: <how to fix it>
                
                List ALL issues found. Use these severity levels:
                  [ERROR]   - compilation error, runtime exception, null pointer, logic bug
                  [WARNING] - potential bug, bad practice, resource leak, race condition
                  [INFO]    - style suggestion, performance improvement, readability tip
                
                If no issues are found, write:
                  [INFO] No issues found — code looks clean.
                
                // === FIXED CODE ===
                <paste the complete corrected %s code here, with all issues resolved>
                
                // === SUMMARY ===
                <one sentence summarizing the overall code quality and main issue category>
                
                Do NOT use markdown. Output plain text only.
                """.formatted(langDisplay, langDisplay);
    }

    // ── User messages ─────────────────────────────────────────────────────────

    /**
     * Builds the user turn message for code generation.
     */
    public String buildGenerateUserMessage(String language, String prompt) {
        return "Generate %s code for: %s".formatted(capitalize(language), prompt);
    }

    /**
     * Builds the user turn message for debug mode.
     */
    public String buildDebugUserMessage(String language, String code, String context) {
        StringBuilder sb = new StringBuilder();
        if (context != null && !context.isBlank()) {
            sb.append("Context (what this code should do): ").append(context).append("\n\n");
        }
        sb.append("Debug the following ").append(capitalize(language)).append(" code:\n\n");
        sb.append(code);
        return sb.toString();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return switch (s.toLowerCase()) {
            case "java"   -> "Java";
            case "python" -> "Python";
            default       -> s.substring(0, 1).toUpperCase() + s.substring(1);
        };
    }
}
