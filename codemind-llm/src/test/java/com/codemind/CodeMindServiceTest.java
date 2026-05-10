package com.codemind;

import com.codemind.model.CodeRequest;
import com.codemind.model.CodeResponse;
import com.codemind.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CodeMindService — exercises validation logic
 * without making real API calls.
 */
class CodeMindServiceTest {

    private NotificationService notificationService;
    private PromptBuilderService promptBuilderService;

    @BeforeEach
    void setUp() {
        notificationService  = new NotificationService();
        promptBuilderService = new PromptBuilderService();
    }

    // ---- PromptBuilderService tests ----

    @Test
    void testGenerateSystemPromptContainsPython() {
        String prompt = promptBuilderService.buildSystemPrompt("generate", "python");
        assertTrue(prompt.contains("Python"), "System prompt should mention Python");
        assertTrue(prompt.contains("PEP 8"),  "Python prompt should reference PEP 8");
    }

    @Test
    void testGenerateSystemPromptContainsJava() {
        String prompt = promptBuilderService.buildSystemPrompt("generate", "java");
        assertTrue(prompt.contains("Java"),   "System prompt should mention Java");
        assertTrue(prompt.contains("Javadoc"), "Java prompt should reference Javadoc");
    }

    @Test
    void testDebugSystemPromptContainsSections() {
        String prompt = promptBuilderService.buildSystemPrompt("debug", "java");
        assertTrue(prompt.contains("DEBUG REPORT"), "Debug prompt should include report section");
        assertTrue(prompt.contains("FIXED CODE"),   "Debug prompt should include fixed code section");
        assertTrue(prompt.contains("SUMMARY"),      "Debug prompt should include summary section");
    }

    @Test
    void testGenerateUserMessageFormat() {
        String msg = promptBuilderService.buildGenerateUserMessage("java", "Implement a stack");
        assertTrue(msg.contains("JAVA"),          "User message should include language");
        assertTrue(msg.contains("Implement a stack"), "User message should include the prompt");
    }

    @Test
    void testDebugUserMessageIncludesCode() {
        String code = "public class Foo { }";
        String msg  = promptBuilderService.buildDebugUserMessage("java", code, "Should compile cleanly");
        assertTrue(msg.contains(code),               "Debug message should include the submitted code");
        assertTrue(msg.contains("Should compile"),   "Debug message should include context");
    }

    // ---- NotificationService tests ----

    @Test
    void testStartSessionCreatesInitialEvent() {
        var events = notificationService.startSession("generate", "python");
        assertFalse(events.isEmpty(), "Should have at least one startup notification");
        assertEquals("info", events.get(0).getType());
    }

    @Test
    void testErrorNotificationAdded() {
        var events = notificationService.startSession("debug", "java");
        notificationService.notifyError(events, "Something went wrong");
        assertTrue(events.stream().anyMatch(e -> "error".equals(e.getType())));
    }

    @Test
    void testSuccessNotificationAdded() {
        var events = notificationService.startSession("generate", "python");
        notificationService.notifyGenerationComplete(events, 42, "python");
        assertTrue(events.stream().anyMatch(e -> "success".equals(e.getType())));
    }

    // ---- CodeRequest model tests ----

    @Test
    void testCodeRequestGettersSetters() {
        CodeRequest req = new CodeRequest("generate", "java", "Write hello world", null);
        assertEquals("generate", req.getMode());
        assertEquals("java",     req.getLanguage());
        assertNotNull(req.getPrompt());
        assertNull(req.getCode());

        req.setCode("public class Hello {}");
        assertNotNull(req.getCode());
    }

    // ---- CodeResponse error factory ----

    @Test
    void testCodeResponseErrorFactory() {
        var events = notificationService.startSession("debug", "python");
        CodeResponse res = CodeResponse.error("Test error", events);
        assertFalse(res.isSuccess());
        assertEquals("Test error", res.getErrorMessage());
        assertNotNull(res.getNotifications());
    }
}
