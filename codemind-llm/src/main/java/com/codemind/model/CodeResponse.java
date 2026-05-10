package com.codemind.model;

import java.util.List;

/**
 * Response DTO returned to the frontend after an LLM call.
 */
public class CodeResponse {

    private boolean success;
    private String  output;
    private String  language;
    private String  mode;
    private int     lineCount;
    private List<NotificationEvent> notifications;
    private DebugSummary debugSummary;
    private String  errorMessage;

    // ---- Inner: notification event ----
    public static class NotificationEvent {
        private String type;   // info | success | warning | error | processing
        private String message;
        private long   timestamp;

        public NotificationEvent(String type, String message) {
            this.type = type;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        public String getType()      { return type; }
        public String getMessage()   { return message; }
        public long   getTimestamp() { return timestamp; }
    }

    // ---- Inner: debug summary ----
    public static class DebugSummary {
        private int errors;
        private int warnings;
        private int infos;
        private String fixedCode;

        public DebugSummary(int errors, int warnings, int infos, String fixedCode) {
            this.errors = errors;
            this.warnings = warnings;
            this.infos = infos;
            this.fixedCode = fixedCode;
        }

        public int    getErrors()    { return errors; }
        public int    getWarnings()  { return warnings; }
        public int    getInfos()     { return infos; }
        public String getFixedCode() { return fixedCode; }
    }

    // ---- Constructors ----
    public CodeResponse() {}

    public static CodeResponse error(String message, List<NotificationEvent> notifications) {
        CodeResponse r = new CodeResponse();
        r.success = false;
        r.errorMessage = message;
        r.notifications = notifications;
        return r;
    }

    // ---- Getters / Setters ----
    public boolean      isSuccess()        { return success; }
    public void         setSuccess(boolean success) { this.success = success; }

    public String       getOutput()        { return output; }
    public void         setOutput(String output) { this.output = output; }

    public String       getLanguage()      { return language; }
    public void         setLanguage(String language) { this.language = language; }

    public String       getMode()          { return mode; }
    public void         setMode(String mode) { this.mode = mode; }

    public int          getLineCount()     { return lineCount; }
    public void         setLineCount(int lineCount) { this.lineCount = lineCount; }

    public List<NotificationEvent> getNotifications() { return notifications; }
    public void setNotifications(List<NotificationEvent> notifications) { this.notifications = notifications; }

    public DebugSummary getDebugSummary()  { return debugSummary; }
    public void         setDebugSummary(DebugSummary debugSummary) { this.debugSummary = debugSummary; }

    public String       getErrorMessage()  { return errorMessage; }
    public void         setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
