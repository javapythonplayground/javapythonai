package com.codemind;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CodeMind LLM — Main Application Entry Point
 *
 * An AI-powered code generation and debugging assistant that supports
 * Java and Python through the Anthropic Claude API.
 *
 * Prerequisites:
 *   - Java 17+
 *   - Maven 3.8+
 *   - Set environment variable: ANTHROPIC_API_KEY=your_key_here
 *
 * Run:  mvn spring-boot:run
 * Open: http://localhost:8080
 */
@SpringBootApplication
public class CodeMindApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeMindApplication.class, args);
        System.out.println("\n==========================================");
        System.out.println("  ██████╗ ██████╗ ██████╗ ███████╗");
        System.out.println("  CodeMind LLM is RUNNING!");
        System.out.println("  URL: http://localhost:8080");
        System.out.println("==========================================\n");
    }
}
