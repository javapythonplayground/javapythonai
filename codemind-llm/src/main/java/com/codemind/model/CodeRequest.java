package com.codemind.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request DTO for code generation and debug operations.
 */
public class CodeRequest {

    @NotBlank(message = "Mode is required")
    @Pattern(regexp = "generate|debug", message = "Mode must be 'generate' or 'debug'")
    private String mode;

    @NotBlank(message = "Language is required")
    @Pattern(regexp = "java|python", message = "Language must be 'java' or 'python'")
    private String language;

    /** Natural-language description (generate) or optional context (debug) */
    private String prompt;

    /** Source code to debug — required when mode = "debug" */
    private String code;

    public CodeRequest() {}

    public CodeRequest(String mode, String language, String prompt, String code) {
        this.mode = mode;
        this.language = language;
        this.prompt = prompt;
        this.code = code;
    }

    public String getMode()     { return mode; }
    public void   setMode(String mode) { this.mode = mode; }

    public String getLanguage()     { return language; }
    public void   setLanguage(String language) { this.language = language; }

    public String getPrompt()     { return prompt; }
    public void   setPrompt(String prompt) { this.prompt = prompt; }

    public String getCode()     { return code; }
    public void   setCode(String code) { this.code = code; }
}
