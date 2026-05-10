package com.codemind.service;

import org.springframework.stereotype.Service;

/**
 * PromptBuilderService
 *
 * Builds the system and user prompts sent to the Anthropic Claude API
 * for both code generation and debug modes, for Java and Python.
 */
@Service
public class PromptBuilderService {

    // ------------------------------------------------------------------ //
    //  SYSTEM PROMPTS
    // ------------------------------------------------------------------ //

    public String buildSystemPrompt(String mode, String language) {
        boolean isPython = "python".equalsIgnoreCase(language);
        return "generate".equalsIgnoreCase(mode)
                ? buildGenerateSystemPrompt(isPython)
                : buildDebugSystemPrompt(isPython);
    }

    private String buildGenerateSystemPrompt(boolean isPython) {
        String lang = isPython ? "Python" : "Java";
        String style = isPython
                ? "Follow PEP 8 style guidelines. Use type hints where appropriate."
                : "Follow standard Java conventions. Use proper access modifiers, generics, and Javadoc.";

        return "You are CodeMind, an expert " + lang + " software engineer.\n\n" +
               "When asked to generate code:\n" +
               "1. Write clean, production-grade " + lang + " code ONLY — no markdown fences.\n" +
               "2. Add inline comments explaining non-obvious logic.\n" +
               "3. " + style + "\n" +
               "4. Handle edge cases and include basic error handling.\n" +
               "5. After the code, add a section starting with exactly:\n" +
               "   // CODEMIND NOTES:\n" +
               "   List 2-3 key design decisions made.\n\n" +
               "Output format: raw code then the notes section. No extra prose.";
    }

    private String buildDebugSystemPrompt(boolean isPython) {
        String lang = isPython ? "Python" : "Java";

        return "You are CodeMind Debugger, an expert " + lang + " code analyst.\n\n" +
               "Analyse the provided code and respond in EXACTLY this structure:\n\n" +
               "// === CODEMIND DEBUG REPORT ===\n" +
               "List every issue found in this format:\n" +
               "  [ERROR|WARNING|INFO] Line <n>: <description> — Fix: <how to fix>\n\n" +
               "// === FIXED CODE ===\n" +
               "Provide the fully corrected " + lang + " code.\n\n" +
               "// === SUMMARY ===\n" +
               "One-line summary of the overall code quality.\n\n" +
               "Rules:\n" +
               "- Plain text only — no markdown.\n" +
               "- If no issues found, write: No issues detected. Code is correct.\n" +
               "- Be specific about line numbers when possible.";
    }

    // ------------------------------------------------------------------ //
    //  USER MESSAGES
    // ------------------------------------------------------------------ //

    public String buildGenerateUserMessage(String language, String prompt) {
        return "Generate " + language.toUpperCase() + " code for the following requirement:\n\n" + prompt;
    }

    public String buildDebugUserMessage(String language, String code, String context) {
        StringBuilder sb = new StringBuilder();
        sb.append("Analyse and debug this ").append(language.toUpperCase()).append(" code:\n\n");
        if (context != null && !context.isBlank()) {
            sb.append("Context / expected behaviour: ").append(context).append("\n\n");
        }
        sb.append("Code:\n").append(code);
        return sb.toString();
    }
}
