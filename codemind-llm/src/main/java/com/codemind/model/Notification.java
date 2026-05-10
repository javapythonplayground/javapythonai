package com.codemind.model;

import java.time.Instant;

/**
 * Notification — represents a single action-log entry emitted during
 * code generation or debugging. These are returned in the response and
 * also streamed via Server-Sent Events (SSE) for real-time UI updates.
 *
 * Types:
 *   INFO       - general information (blue dot in UI)
 *   SUCCESS    - operation completed successfully (green dot)
 *   WARNING    - potential issue or caution (amber dot)
 *   ERROR      - operation failed or critical issue found (red dot)
 *   PROCESSING - currently running step (purple pulsing dot)
 */
public class Notification {

    public enum Type {
        INFO, SUCCESS, WARNING, ERROR, PROCESSING
    }

    private String message;
    private Type type;
    private Instant timestamp;
    private String step;       // optional machine-readable step name

    public Notification() {
        this.timestamp = Instant.now();
    }

    public Notification(String message, Type type) {
        this.message = message;
        this.type = type;
        this.timestamp = Instant.now();
    }

    public Notification(String message, Type type, String step) {
        this(message, type);
        this.step = step;
    }

    // ── Static factory shortcuts ──────────────────────────────────────────────

    public static Notification info(String message) {
        return new Notification(message, Type.INFO);
    }

    public static Notification info(String message, String step) {
        return new Notification(message, Type.INFO, step);
    }

    public static Notification success(String message) {
        return new Notification(message, Type.SUCCESS);
    }

    public static Notification warning(String message) {
        return new Notification(message, Type.WARNING);
    }

    public static Notification error(String message) {
        return new Notification(message, Type.ERROR);
    }

    public static Notification processing(String message) {
        return new Notification(message, Type.PROCESSING);
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public String getStep() { return step; }
    public void setStep(String step) { this.step = step; }

    @Override
    public String toString() {
        return "[" + type + "] " + message;
    }
}
