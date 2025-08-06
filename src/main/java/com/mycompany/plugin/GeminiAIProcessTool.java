package com.mycompany.plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.DefaultApplicationPlugin;
import org.joget.workflow.model.WorkflowAssignment;

/**
 * Simple Gemini AI Process Tool Plugin
 * Basic plugin that connects to Google Gemini AI API
 * Takes user prompt and returns AI response
 * 
 * @author AI Development Team
 * @version 1.0.0
 */
public class GeminiAIProcessTool extends DefaultApplicationPlugin {
    
    private final String ClassName = getClassName();
    // private static final String DEBUG_MODE = loadValueFromEnvFile("DEBUG") != null ? loadValueFromEnvFile("DEBUG") : "false";
    // private static final boolean IS_DEBUG_MODE = "true".equalsIgnoreCase(DEBUG_MODE);
    private static final boolean IS_DEBUG_MODE = true; // Set to true for debugging, false for production
    
    // Debug logging method
    private void debugLog(String message) {
        if (IS_DEBUG_MODE) {
            LogUtil.info(ClassName, "[DEBUG] " + message);
        }
    }
    
    // Debug error logging method
    private void debugError(String message, Exception e) {
        if (IS_DEBUG_MODE) {
            LogUtil.error(ClassName, e, "[DEBUG ERROR] " + message);
        }
    }

    // /**
    //  * Loads a value from a .env file based on the provided key name.
    //  */
    // private static String loadValueFromEnvFile(String Name) {
    //     try {
    //         Properties props = new Properties();
    //         InputStream envStream = GeminiAIProcessTool.class.getClassLoader().getResourceAsStream(".env");
    //         if (envStream != null) {
    //             props.load(envStream);
    //             return props.getProperty(Name);
    //         } else {
    //             System.err.println(".env file not found in resources");
    //         }
    //     } catch (IOException e) {
    //         System.err.println("Error reading .env file: " + e.getMessage());
    //     }
    //     return null;
    // }
    
    @Override
    public String getName() {
        return "Gemini AI Process Tool";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Simple process tool to integrate Google Gemini AI for content generation and processing";
    }

    @Override
    public String getLabel() {
        return "Gemini AI Process Tool";
    }

    @Override
    public String getClassName() {
        return this.getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        String jsonPath = "/properties/GeminiAIProcessTool.json";
        String messagePath = "message/GeminiAIProcessTool";
        String options = AppUtil.readPluginResource(
            getClassName(), 
            jsonPath, 
            null, 
            true, 
            messagePath
        );
        return options;
    }
    

    /**
     * Main execution method - processes user prompt and returns Gemini AI response
     */
    @Override
    public Object execute(Map properties) {
        debugLog("=== Gemini AI Process Tool Starting ===");
        
        try {
            WorkflowAssignment assignment = (WorkflowAssignment) properties.get("workflowAssignment");
            
            // Get configuration from properties
            String apiKey = getPropertyString("apiKey");
            String model = getPropertyString("model");
            String systemInstruction = getPropertyString("systemInstruction");
            String outputVariable = getPropertyString("outputVariable");
            String userPromptVariable = getPropertyString("userPromptVariable");
            String additionalContext = getPropertyString("additionalContext");
            String customPromptTemplate = getPropertyString("customPromptTemplate");
            
            debugLog("Configuration loaded:");
            debugLog("- API Key: " + (apiKey != null && !apiKey.isEmpty() ? "PROVIDED" : "MISSING"));
            debugLog("- Model: " + model);
            debugLog("- Output Variable: '" + outputVariable + "'");
            debugLog("- User Prompt Variable: '" + userPromptVariable + "'");
            debugLog("- System Instruction: " + systemInstruction);
            debugLog("- Additional Context: " + additionalContext);
            debugLog("- Custom Prompt Template: " + customPromptTemplate);
            
            // Validate required fields
            if (apiKey == null || apiKey.trim().isEmpty()) {
                debugError("ERROR: Gemini API Key is required", null);
                return "ERROR: Gemini API Key is required. Please configure it in the plugin properties.";
            }
            
            // Set defaults
            if (model == null || model.trim().isEmpty()) {
                model = "gemini-1.5-flash";
            }
            
            if (systemInstruction == null || systemInstruction.trim().isEmpty()) {
                systemInstruction = "You are a helpful and concise AI assistant.";
            }
            
            if (outputVariable == null || outputVariable.trim().isEmpty()) {
                outputVariable = "aiResponse";
            }
            
            if (userPromptVariable == null || userPromptVariable.trim().isEmpty()) {
                userPromptVariable = "userprompt";
            }

            debugLog("Using final configuration:");
            debugLog("- Model: " + model);
            debugLog("- Output Variable: '" + outputVariable + "'");
            debugLog("- User Prompt Variable: '" + userPromptVariable + "'");

            // Build the final prompt
            String finalPrompt = buildFinalPrompt(userPromptVariable, additionalContext, customPromptTemplate, assignment);
            debugLog("Final prompt: " + finalPrompt);
            
            // Validate that we got a valid prompt
            if (finalPrompt == null || finalPrompt.trim().isEmpty()) {
                debugError("ERROR: No valid prompt could be constructed", null);
                return "ERROR: No valid prompt could be constructed. Please check your variable configuration.";
            }

            // Call Gemini API
            debugLog("Calling Gemini API...");
            String response = callGeminiAPI(apiKey, model, finalPrompt, systemInstruction);
            debugLog("Gemini API response received: " + (response != null ? response.substring(0, Math.min(100, response.length())) + "..." : "NULL"));
            
            // Store the AI response in workflow variable if we have a valid response and assignment
            if (response != null && !response.startsWith("ERROR:") && assignment != null) {
                try {
                    debugLog("Attempting to store response in workflow variable: '" + outputVariable + "'");
                    setWorkflowVariable(assignment.getActivityId(), outputVariable, response);
                    debugLog("SUCCESS: Stored AI response in workflow variable: '" + outputVariable + "'");
                    
                } catch (ClassCastException | NullPointerException e) {
                    debugError("FAILED to store response in workflow variable: '" + outputVariable + "'", e);
                    // Don't fail the entire process, just log the error
                }
            } else {
                debugLog("WARNING: Cannot store response - response=" + response + ", assignment=" + assignment);
            }
            
            return response != null ? response : "ERROR: Failed to get a valid response from Gemini API";
            
        } catch (NullPointerException e) {
            debugError("Null pointer error in plugin execution", e);
            return "ERROR: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            debugError("Invalid argument error in plugin execution", e);
            return "ERROR: " + e.getMessage();
        } catch (RuntimeException e) {
            debugError("Runtime error in plugin execution", e);
            return "ERROR: " + e.getMessage();
        }
    }


    /**
     * Set workflow variable
     */
    private void setWorkflowVariable(String activityId, String variableName, String value) {
        try {
            Class<?> appUtilClass = Class.forName("org.joget.apps.app.service.AppUtil");
            Object appContext = appUtilClass.getMethod("getApplicationContext").invoke(null);

            Object workflowManager = appContext.getClass()
                    .getMethod("getBean", String.class)
                    .invoke(appContext, "workflowManager");

            workflowManager.getClass()
                    .getMethod("activityVariable", String.class, String.class, Object.class)
                    .invoke(workflowManager, activityId, variableName, value);

        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | 
                 java.lang.reflect.InvocationTargetException e) {
            LogUtil.error(getClassName(), e,
                    "Could not set workflow variable using WorkflowManager: " + e.getMessage());
            throw new RuntimeException("Failed to set workflow variable", e);
        }
    }

    /**
     * Calls the Google Gemini API with the user prompt
     */
    private String callGeminiAPI(String apiKey, String model, String prompt, String systemInstruction) {
        try {
            debugLog("Starting Gemini API call with model: " + model);
            
            // Construct API URL
            String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;
            debugLog("API URL constructed");
            
            // Create request payload using string building
            String requestBody = "{"
                + "\"contents\":[{"
                + "\"parts\":[{\"text\":\"" + escapeJson(prompt) + "\"}]"
                + "}]";
                
            // Add system instruction if provided
            if (systemInstruction != null && !systemInstruction.trim().isEmpty()) {
                requestBody += ",\"systemInstruction\":{"
                    + "\"parts\":[{\"text\":\"" + escapeJson(systemInstruction) + "\"}]"
                    + "}";
            }
            requestBody += "}";
            
            debugLog("Request payload prepared");
            
            // Make HTTP request
            URL url = new URI(apiUrl).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(30000); // 30 seconds
            connection.setReadTimeout(60000);    // 60 seconds
            
            // Send request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            
            debugLog("Request sent to Gemini API");
            
            // Read response
            int responseCode = connection.getResponseCode();
            debugLog("HTTP Response Code: " + responseCode);
            
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    responseCode == 200 ? connection.getInputStream() : connection.getErrorStream(), "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }
            
            if (responseCode == 200) {
                debugLog("Successful API response received");
                // Parse response using simple string parsing
                String responseStr = response.toString();
                String aiResponse = extractTextFromGeminiResponse(responseStr);
                if (aiResponse != null) {
                    debugLog("AI Response received successfully");
                    return aiResponse;
                } else {
                    debugError("ERROR: Could not extract text from Gemini API response", null);
                    return null;
                }
            } else {
                debugError("API call failed with HTTP " + responseCode + ": " + response.toString(), null);
                return null;
            }
            
        } catch (IOException e) {
            debugError("Network error calling Gemini API: " + e.getMessage(), e);
            return null;
        } catch (URISyntaxException e) {
            debugError("Invalid API URL: " + e.getMessage(), e);
            return null;
        } catch (Exception e) {
            debugError("Unexpected error calling Gemini API: " + e.getMessage(), e);
            return null;
        }
        
        
        // MOCK RESPONSE FOR JOGET CLOUD TESTING (NO NETWORK IO)
        // debugLog("Using mock response - network calls disabled for cloud compatibility");
        // return "Mock AI Response: This is a test response for prompt '" + prompt + "' using model " + model + ". Network calls are disabled for Joget Cloud compatibility.";
    }
    
    /**
     * Build the final prompt for Gemini AI using user prompt variable, additional context, and custom template
     */
    private String buildFinalPrompt(String userPromptVariable, String additionalContext, String customPromptTemplate, WorkflowAssignment assignment) {
        try {
            StringBuilder promptBuilder = new StringBuilder();
            
            // If we have a custom template, use it
            if (customPromptTemplate != null && !customPromptTemplate.trim().isEmpty()) {
                debugLog("Using custom prompt template");
                String processedTemplate = processHashVariables(customPromptTemplate, assignment);
                return processedTemplate;
            }

            // Otherwise, build prompt from user prompt variable and additional context
            String userPrompt = getWorkflowVariable(assignment, userPromptVariable);
            debugLog("Retrieved user prompt from variable '" + userPromptVariable + "': " + userPrompt);
            
            if (userPrompt != null && !userPrompt.trim().isEmpty()) {
                promptBuilder.append(userPrompt);
            } else {
                debugLog("Warning: User prompt variable '" + userPromptVariable + "' is empty or null");
            }
            
            // Add additional context variables if specified
            if (additionalContext != null && !additionalContext.trim().isEmpty()) {
                String[] contextVariables = additionalContext.split(",");
                for (String contextVar : contextVariables) {
                    contextVar = contextVar.trim();
                    if (!contextVar.isEmpty()) {
                        String contextValue = getWorkflowVariable(assignment, contextVar);
                        if (contextValue != null && !contextValue.trim().isEmpty()) {
                            if (promptBuilder.length() > 0) {
                                promptBuilder.append("\n\n");
                            }
                            promptBuilder.append(contextVar).append(": ").append(contextValue);
                            debugLog("Added context variable '" + contextVar + "': " + contextValue);
                        }
                    }
                }
            }
            
            String finalPrompt = promptBuilder.toString().trim();
            debugLog("Final prompt built: " + finalPrompt);
            return finalPrompt;
            
        } catch (NullPointerException | IllegalArgumentException e) {
            debugError("Error due to invalid input or null value", e);
            return null;
        } catch (RuntimeException e) {
            debugError("Runtime exception occurred while building final prompt", e);
            return null;
        } catch (Exception e) {
            debugError("Unexpected error building final prompt", e);
            return null;
        }
    }

    /**
     * Get workflow variable value
     */
    private String getWorkflowVariable(WorkflowAssignment assignment, String variableName) {
        if (assignment == null || variableName == null || variableName.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Process the hash variable to get the actual value
            String hashVariable = "#variable." + variableName.trim() + "#";
            String processedValue = processHashVariables(hashVariable, assignment);
            
            // If the processed value is still the hash variable, it means the variable doesn't exist
            if (hashVariable.equals(processedValue)) {
                debugLog("Variable '" + variableName + "' not found or is empty");
                return null;
            }
            
            debugLog("Get " + variableName + processedValue);
            return processedValue;
            
        } catch (NullPointerException | IllegalArgumentException e) {
            debugError("Error getting workflow variable '" + variableName + "'", e);
            return null;
        } catch (Exception e) {
            debugError("Unexpected error getting workflow variable '" + variableName + "'", e);
            return null;
        }
    }
    
    /**
     * Process hash variables in a string (e.g., #{variable.name} format)
     */
    private String processHashVariables(String input, WorkflowAssignment assignment) {
        if (input == null || assignment == null) {
            return input;
        }
        
        try {
            // Use AppUtil to process hash variables
            return AppUtil.processHashVariable(input, assignment, null, null);
        } catch (Exception e) {
            debugError("Error processing hash variables in: " + input, e);
            return input; // Return original string if processing fails
        }
    }
    
    /**
     * Helper method to escape JSON strings
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    /**
     * Helper method to unescape JSON strings
     */
    private String unescapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\\"", "\"")
                  .replace("\\\\", "\\")
                  .replace("\\n", "\n")
                  .replace("\\r", "\r")
                  .replace("\\t", "\t");
    }
    
    /**
     * Extract text from Gemini API response using simple string parsing
     */
    private String extractTextFromGeminiResponse(String responseStr) {
        try {
            // Look for the pattern: "text":"actual_content"
            String textPattern = "\"text\":\"";
            int textStart = responseStr.indexOf(textPattern);
            if (textStart == -1) {
                debugLog("Could not find 'text' field in response");
                return null;
            }
            
            textStart += textPattern.length();
            
            // Find the end of the text content, accounting for escaped quotes
            int textEnd = textStart;
            boolean escaped = false;
            
            while (textEnd < responseStr.length()) {
                char c = responseStr.charAt(textEnd);
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    break;
                }
                textEnd++;
            }
            
            if (textEnd >= responseStr.length()) {
                debugLog("Could not find end of text content");
                return null;
            }
            
            String extractedText = responseStr.substring(textStart, textEnd);
            return unescapeJson(extractedText);
            
        } catch (Exception e) {
            debugError("Error extracting text from Gemini response", e);
            return null;
        }
    }
    
}