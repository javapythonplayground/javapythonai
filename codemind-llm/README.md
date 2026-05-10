# CodeMind LLM — Java Spring Boot

> AI-powered **code generation** and **debugging** assistant for Java and Python,
> powered by Anthropic Claude. Built with Spring Boot 3 + WebFlux.

---

## Project Structure

```
codemind-llm/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/codemind/
    │   │   ├── CodeMindApplication.java          ← Spring Boot entry point
    │   │   ├── config/
    │   │   │   ├── AnthropicConfig.java           ← WebClient + API key config
    │   │   │   └── WebConfig.java                 ← CORS configuration
    │   │   ├── controller/
    │   │   │   └── CodeMindController.java         ← REST endpoints
    │   │   ├── model/
    │   │   │   ├── CodeRequest.java                ← Request DTO
    │   │   │   ├── CodeResponse.java               ← Response DTO + Notification events
    │   │   │   ├── AnthropicApiRequest.java        ← Anthropic API payload
    │   │   │   └── AnthropicApiResponse.java       ← Anthropic API response mapping
    │   │   └── service/
    │   │       ├── CodeMindService.java             ← Orchestration layer
    │   │       ├── AnthropicClientService.java      ← HTTP calls to Claude API
    │   │       ├── PromptBuilderService.java        ← System + user prompt construction
    │   │       ├── NotificationService.java         ← Real-time action event tracking
    │   │       └── DebugAnalyserService.java        ← Debug report parser
    │   └── resources/
    │       ├── application.properties
    │       └── static/
    │           └── index.html                       ← Full frontend UI
    └── test/
        └── java/com/codemind/
            ├── CodeMindServiceTest.java
            └── DebugAnalyserServiceTest.java
```

---

## Prerequisites

| Tool  | Version |
|-------|---------|
| Java  | 17+     |
| Maven | 3.8+    |
| Anthropic API Key | [Get one here](https://console.anthropic.com/) |

---

## Quick Start

### 1. Set your API key

**Option A — Environment variable (recommended)**
```bash
export ANTHROPIC_API_KEY=sk-ant-your-key-here
```

**Option B — application.properties**
```properties
anthropic.api.key=sk-ant-your-key-here
```

### 2. Build and run

```bash
cd codemind-llm
mvn spring-boot:run
```

### 3. Open the UI

```
http://localhost:8080
```

---

## REST API Reference

### Health Check
```
GET /api/health
```

### Generate Code
```
POST /api/code/generate
Content-Type: application/json

{
  "mode": "generate",
  "language": "java",
  "prompt": "Write a thread-safe queue using LinkedList"
}
```

### Debug Code
```
POST /api/code/debug
Content-Type: application/json

{
  "mode": "debug",
  "language": "python",
  "prompt": "This function should return sorted unique values",
  "code": "def process(data):\n    return data"
}
```

### Response Format
```json
{
  "success": true,
  "output": "...generated or debug report...",
  "language": "java",
  "mode": "generate",
  "lineCount": 42,
  "notifications": [
    { "type": "info",       "message": "Session started — mode: generate, language: JAVA", "timestamp": 1234567890 },
    { "type": "processing", "message": "Sending request to Anthropic API...",              "timestamp": 1234567891 },
    { "type": "success",    "message": "Code generation complete — 42 lines of JAVA.",     "timestamp": 1234567892 }
  ],
  "debugSummary": null
}
```

#### Notification types
| Type         | Meaning                        |
|--------------|-------------------------------|
| `info`       | Informational step             |
| `processing` | In-progress async operation    |
| `success`    | Step completed successfully    |
| `warning`    | Non-critical issue detected    |
| `error`      | Critical error encountered     |

---

## Run Tests

```bash
mvn test
```

---

## Configuration Reference (`application.properties`)

| Property                  | Default                           | Description                  |
|---------------------------|-----------------------------------|------------------------------|
| `anthropic.api.key`       | `${ANTHROPIC_API_KEY}`            | Your Anthropic secret key    |
| `anthropic.model`         | `claude-sonnet-4-20250514`        | Claude model to use          |
| `anthropic.max.tokens`    | `2048`                            | Max tokens per response      |
| `server.port`             | `8080`                            | HTTP server port             |

---

## Build JAR

```bash
mvn clean package
java -jar target/codemind-llm-1.0.0.jar
```
