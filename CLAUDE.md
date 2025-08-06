# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Joget Workflow plugin that integrates Google Gemini AI for intelligent content generation and processing within business workflows. The plugin allows users to send prompts to Google's Gemini AI API and store AI-generated responses in workflow variables.

**Plugin Type**: OSGi Bundle for Joget Workflow Platform (v8.2.0)
**Architecture**: Standard Joget process tool plugin extending `DefaultApplicationPlugin`

## Build Commands

### Primary Build Command
```bash
mvn clean package
```

### Quick Development Build & Deploy (Windows)
```bash
update.bat
```
The `update.bat` script builds the plugin and automatically deploys it to the local Joget instance at `C:\Joget-DX8-Enterprise\wflow\app_plugins`.

### Manual Build Steps
1. Compile: `mvn clean compile`
2. Package: `mvn package` 
3. Deploy: Copy `target/gemini-ai-process-tool-1.0.0.jar` to Joget's `app_plugins` directory

## Core Architecture

### Main Components

1. **GeminiAIProcessTool.java** (`src/main/java/com/mycompany/plugin/GeminiAIProcessTool.java`)
   - Extends `DefaultApplicationPlugin` 
   - Core plugin logic for Gemini AI integration
   - Handles workflow variable processing and API communication

2. **Activator.java** (`src/main/java/com/mycompany/plugin/Activator.java`)
   - OSGi bundle activator for plugin registration
   - Implements `BundleActivator` interface

3. **Configuration Files**
   - `properties/GeminiAIProcessTool.json` - Plugin UI configuration schema
   - `message/GeminiAIProcessTool.properties` - Internationalization properties

### Key Design Patterns

**Workflow Variable Integration**: The plugin extensively uses Joget's hash variable processing (`#{variable.name}`) to:
- Read input prompts from workflow variables
- Include additional context from multiple variables
- Store AI responses back to workflow variables
- Support custom prompt templates with variable substitution

**API Integration Pattern**: 
- HTTP POST requests to Google Gemini API
- JSON request/response handling using `org.json` library
- Comprehensive error handling and timeout management
- Support for multiple Gemini models (1.5 Flash, 1.5 Pro, 2.0 Flash, 2.0 Pro)

**Configuration Management**:
- Plugin properties loaded via `getPropertyString()` method
- Secure API key storage using Joget's password field type
- Default value fallbacks for optional configurations

### Workflow Variable Flow

1. **Input Variables** (configured in plugin properties):
   - `userPromptVariable` - Primary prompt source (default: "userprompt")
   - `additionalContext` - Comma-separated context variables
   - `customPromptTemplate` - Template with `#{variable.name}` references

2. **Processing**:
   - Variables retrieved using `getWorkflowVariable()` method
   - Hash variable processing via `AppUtil.processHashVariable()`
   - Prompt construction in `buildFinalPrompt()` method

3. **Output**:
   - AI response stored in `outputVariable` (default: "aiResponse")
   - Uses reflection-based `setWorkflowVariable()` for workflow integration

### Error Handling Strategy

- Comprehensive validation of required fields (API key, prompts)
- Network timeout handling (30s connect, 60s read)
- JSON parsing error recovery
- Graceful degradation when workflow variables are missing
- Debug logging system controlled by `joget.debug` system property

## Development Environment Setup

### Prerequisites
- Java 8+ (configured for Java 8 compilation)
- Maven 3.x
- Joget Workflow v8.2.0+
- Google Cloud Account with Gemini API access

### Local Development
The project includes a Windows batch script (`update.bat`) for rapid development cycles that:
1. Builds the plugin using Maven
2. Removes old plugin file from Joget
3. Copies new JAR to Joget plugins directory
4. Provides success/error feedback

### Configuration Requirements
All Joget workflow variables referenced in the plugin must exist in the workflow process before plugin execution. The plugin performs no workflow variable creation - only reads existing variables and writes to the output variable.

## Dependencies & External Services

### Maven Dependencies
- **Joget Framework** (v8.2.0) - `wflow-core`, `wflow-plugin-base`, `wflow-wfengine`, `wflow-commons`
- **OSGi Framework** (v6.0.0) - Bundle lifecycle management
- **JSON Processing** (org.json 20230227) - API request/response handling
- **Spring Framework** (v5.3.21) - Provided by Joget platform

### External API
- **Google Gemini API** - Requires valid API key and internet connectivity
- Endpoint: `https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent`
- Supported models: gemini-1.5-flash, gemini-1.5-pro, gemini-2.0-flash, gemini-2.0-pro

## Testing & Debugging

### Debug Mode
Enable debug logging by setting system property:
```
-Djoget.debug=true
```

Debug logs include:
- Plugin configuration validation
- Workflow variable retrieval details  
- API request/response information
- Error diagnostics with stack traces

### Common Integration Points
- Workflow variable validation: Ensure all referenced variables exist before plugin execution
- API quota management: Monitor Gemini API usage limits
- Network connectivity: Plugin requires HTTPS access to Google services
- Plugin deployment: Restart Joget after plugin updates for OSGi bundle refresh