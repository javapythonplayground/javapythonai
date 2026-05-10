package com.codemind;

import com.codemind.model.CodeRequest;
import com.codemind.model.DebugIssue;
import com.codemind.service.DebugReportParser;
import com.codemind.service.NotificationService;
import com.codemind.service.PromptBuilder;
import com.codemind.model.Notification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CodeMindApplicationTests — integration and unit tests for the CodeMind LLM backend.
 *
 * Tests cover:
 *   - PromptBuilder system prompt construction
 *   - DebugReportParser issue extraction
 *   - NotificationService event generation
 *   - CodeRequest validation helpers
 */
@SpringBootTest
class CodeMindApplicationTests {

    @Autowired
    private PromptBuilder promptBuilder;

    @Autowired
    private DebugReportParser debugReportParser;

    @Autowired
    private NotificationService notificationService;

    // ── PromptBuilder tests ───────────────────────────────────────────────────

    @Test
    void generateSystemPromptContainsLanguage() {
        String prompt = promptBuilder.buildGenerateSystemPrompt("java");
        assertThat(prompt).contains("Java");
    }

    @Test
    void debugSystemPromptContainsSeverityLevels() {
        String prompt = promptBuilder.buildDebugSystemPrompt("python");
        assertThat(prompt).contains("[ERROR]");
        assertThat(prompt).contains("[WARNING]");
        assertThat(prompt).contains("[INFO]");
    }

    @Test
    void userMessageIncludesPrompt() {
        String msg = promptBuilder.buildGenerateUserMessage("python", "binary search");
        assertThat(msg).contains("binary search");
        assertThat(msg).contains("Python");
    }

    // ── DebugReportParser tests ───────────────────────────────────────────────

    @Test
    void parsesErrorIssueCorrectly() {
        String report = """
                // === CODEMIND DEBUG REPORT ===
                [ERROR] Line 12: NullPointerException possible — Fix: add null check
                // === FIXED CODE ===
                int x = 0;
                // === SUMMARY ===
                One null pointer risk found.
                """;

        List<DebugIssue> issues = debugReportParser.parse(report);
        assertThat(issues).hasSize(1);
        assertThat(issues.get(0).getSeverity()).isEqualTo(DebugIssue.Severity.ERROR);
        assertThat(issues.get(0).getLineNumber()).isEqualTo(12);
        assertThat(issues.get(0).getDescription()).contains("NullPointerException");
        assertThat(issues.get(0).getSuggestion()).contains("null check");
    }

    @Test
    void parsesMultipleIssuesWithDifferentSeverities() {
        String report = """
                // === CODEMIND DEBUG REPORT ===
                [ERROR] Line 5: Division by zero possible — Fix: check divisor
                [WARNING] Line 10: Magic number 42 — Fix: use constant
                [INFO] Line 15: Consider using StringBuilder — Fix: replace + concatenation
                // === FIXED CODE ===
                """;

        List<DebugIssue> issues = debugReportParser.parse(report);
        assertThat(issues).hasSize(3);
        assertThat(issues.get(0).isError()).isTrue();
        assertThat(issues.get(1).isWarning()).isTrue();
        assertThat(issues.get(2).isInfo()).isTrue();
    }

    @Test
    void extractsFixedCode() {
        String report = """
                // === CODEMIND DEBUG REPORT ===
                [INFO] No issues found.
                // === FIXED CODE ===
                public class Foo { }
                // === SUMMARY ===
                Code is clean.
                """;

        String fixedCode = debugReportParser.extractFixedCode(report);
        assertThat(fixedCode).contains("public class Foo");
    }

    @Test
    void extractsSummary() {
        String report = """
                // === CODEMIND DEBUG REPORT ===
                [INFO] No issues.
                // === FIXED CODE ===
                pass
                // === SUMMARY ===
                Overall code quality is good with no critical issues.
                """;

        String summary = debugReportParser.extractSummary(report);
        assertThat(summary).contains("good");
    }

    @Test
    void returnsEmptyListForEmptyInput() {
        List<DebugIssue> issues = debugReportParser.parse("");
        assertThat(issues).isEmpty();
    }

    // ── NotificationService tests ─────────────────────────────────────────────

    @Test
    void generateStartNotificationsContainProcessingStep() {
        List<Notification> notifs = notificationService
                .buildGenerateStartNotifications("java", "build a queue");
        assertThat(notifs).isNotEmpty();
        assertThat(notifs.stream().anyMatch(n -> n.getType() == Notification.Type.PROCESSING)).isTrue();
    }

    @Test
    void debugSuccessNotificationsShowErrorCount() {
        List<DebugIssue> issues = List.of(
                DebugIssue.error(1, "NPE", "add null check"),
                DebugIssue.error(2, "Div by zero", "validate"),
                DebugIssue.warning(3, "Magic number", "use constant")
        );
        List<Notification> notifs = notificationService.buildDebugSuccessNotifications(issues);
        assertThat(notifs.stream()
                .anyMatch(n -> n.getMessage().contains("2 error"))).isTrue();
        assertThat(notifs.stream()
                .anyMatch(n -> n.getMessage().contains("1 warning"))).isTrue();
    }

    @Test
    void cleanCodeProducesNoIssueNotification() {
        List<DebugIssue> noIssues = List.of(
                DebugIssue.info(-1, "No issues found — code looks clean.", "")
        );
        List<Notification> notifs = notificationService.buildDebugSuccessNotifications(noIssues);
        assertThat(notifs.stream()
                .anyMatch(n -> n.getType() == Notification.Type.SUCCESS)).isTrue();
    }

    // ── CodeRequest tests ─────────────────────────────────────────────────────

    @Test
    void codeRequestModeHelpers() {
        CodeRequest gen = new CodeRequest("java", "generate", "build a tree", null);
        assertThat(gen.isGenerateMode()).isTrue();
        assertThat(gen.isDebugMode()).isFalse();

        CodeRequest dbg = new CodeRequest("python", "debug", null, "x = None\nprint(x.foo)");
        assertThat(dbg.isDebugMode()).isTrue();
        assertThat(dbg.isGenerateMode()).isFalse();
    }
}
