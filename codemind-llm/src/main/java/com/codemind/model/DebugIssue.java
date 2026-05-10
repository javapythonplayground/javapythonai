package com.codemind.model;

/**
 * DebugIssue — represents a single issue found during code debugging.
 *
 * Parsed from the LLM's debug report output which follows the format:
 *   [SEVERITY] Line X: description — fix suggestion
 *
 * Severity levels mirror standard IDE inspection levels:
 *   ERROR   - compilation/runtime failure, must fix
 *   WARNING - potential bug, logic flaw, or anti-pattern
 *   INFO    - suggestion, style issue, or best-practice note
 */
public class DebugIssue {

    public enum Severity {
        ERROR, WARNING, INFO
    }

    private Severity severity;
    private int lineNumber;          // -1 if line not identified
    private String description;
    private String suggestion;       // how to fix it
    private String rawText;          // original text from LLM

    public DebugIssue() {
        this.lineNumber = -1;
    }

    public DebugIssue(Severity severity, int lineNumber, String description, String suggestion) {
        this.severity = severity;
        this.lineNumber = lineNumber;
        this.description = description;
        this.suggestion = suggestion;
    }

    // ── Static factory shortcuts ──────────────────────────────────────────────

    public static DebugIssue error(int line, String description, String suggestion) {
        return new DebugIssue(Severity.ERROR, line, description, suggestion);
    }

    public static DebugIssue warning(int line, String description, String suggestion) {
        return new DebugIssue(Severity.WARNING, line, description, suggestion);
    }

    public static DebugIssue info(int line, String description, String suggestion) {
        return new DebugIssue(Severity.INFO, line, description, suggestion);
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public int getLineNumber() { return lineNumber; }
    public void setLineNumber(int lineNumber) { this.lineNumber = lineNumber; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }

    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }

    public boolean isError()   { return Severity.ERROR.equals(this.severity); }
    public boolean isWarning() { return Severity.WARNING.equals(this.severity); }
    public boolean isInfo()    { return Severity.INFO.equals(this.severity); }

    @Override
    public String toString() {
        String loc = lineNumber > 0 ? " Line " + lineNumber + ":" : ":";
        return "[" + severity + "]" + loc + " " + description;
    }
}
