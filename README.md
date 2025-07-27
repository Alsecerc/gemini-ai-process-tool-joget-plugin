# Gemini AI Process Tool Plugin

A Joget Workflow plugin that integrates Google Gemini AI for intelligent content generation and processing within your workflows.

## Overview

This plugin allows you to send user prompts to Google's Gemini AI API and store the AI-generated responses in workflow variables. It's perfect for automating content generation, analysis, and intelligent decision-making within your business processes.

## Features

- 🤖 **Google Gemini AI Integration** - Supports multiple Gemini models (1.5 Flash, 1.5 Pro, 2.0 Flash, 2.0 Pro)
- 📝 **Flexible Prompt Handling** - Use workflow variables or custom templates for prompts
- 🔧 **Configurable System Instructions** - Customize AI behavior and expertise domain
- 📊 **Additional Context** - Include multiple workflow variables as context
- 🔐 **Secure API Key Management** - Encrypted storage of your Gemini API key
- 📋 **Debug Logging** - Comprehensive logging for troubleshooting
- ⚡ **Easy Integration** - Simple drag-and-drop into your workflow processes

## Requirements

- Joget Workflow v7+ 
- Google Cloud Account with Gemini API access
- Valid Gemini API Key

## Installation

1. Build the plugin:
   ```bash
   mvn clean compile package
   ```

2. The compiled JAR file will be available at:
   ```
   target/gemini-ai-process-tool-1.0.0.jar
   ```

3. Upload the JAR file to your Joget instance via **Settings > System Settings > Manage Plugins**

## ⚠️ Important: Workflow Variable Setup

**CRITICAL REQUIREMENT**: All input values (like `userprompt`) must be properly stored in Joget's workflow variables and process variables before using this plugin.

### How to Set Up Workflow Variables

1. **Form Fields**: Configure your form fields to store data in workflow variables
   - In Form Builder, set the **ID** of form elements to match your variable names
   - Example: Text field with ID `userprompt` will store data in workflow variable `userprompt`

2. **Process Variables**: Ensure variables are available at the process level
   - Variables must be accessible throughout the workflow process
   - Use form fields, previous process tools, or manual variable assignment

3. **Variable Naming**: Use simple names without special characters
   - ✅ Good: `userprompt`, `customerName`, `orderAmount`
   - ❌ Avoid: `user-prompt`, `customer.name`, `order_amount`

### Example Workflow Setup

```
Step 1: Form with fields
├── Text Area (ID: userprompt) → stores in workflow variable "userprompt"
├── Text Field (ID: customerName) → stores in workflow variable "customerName"
└── Number Field (ID: orderAmount) → stores in workflow variable "orderAmount"

Step 2: Gemini AI Process Tool
├── User Prompt Variable: userprompt
├── Additional Context Variables: customerName,orderAmount
└── Output Variable Name: aiResponse

Step 3: Display Results
└── Text Area displaying #{variable.aiResponse}
```

## Configuration

### Required Settings

| Field | Description | Default |
|-------|-------------|---------|
| **Gemini API Key** | Your Google Gemini API key (required) | - |
| **AI Model** | Choose from available Gemini models | `gemini-1.5-flash` |

### Input Configuration

| Field | Description | Default | Requirements |
|-------|-------------|---------|--------------|
| **User Prompt Variable** | Workflow variable containing the user prompt | `userprompt` | **Must exist in workflow variables** |
| **Additional Context Variables** | Comma-separated list of variables to include | - | **Each variable must exist in workflow variables** |
| **Custom Prompt Template** | Custom template using `#{variable.name}` syntax | - | **Referenced variables must exist in workflow** |

### Output Configuration

| Field | Description | Default |
|-------|-------------|---------|
| **Output Variable Name** | Variable to store the AI response | `aiResponse` |
| **System Instruction** | Instructions to guide AI behavior | `You are a helpful and concise AI assistant.` |

## Usage Examples

### Basic Usage
1. **Create a form** with a text area field having ID `userprompt`
2. **Ensure the form** properly stores data in workflow variables
3. **Add the Gemini AI Process Tool** to your workflow after the form
4. **Configure the plugin** with your API key
5. **The AI response** will be stored in `aiResponse` workflow variable

### Advanced Usage with Context
```
Prerequisites:
- Form field with ID: userQuestion (stores in workflow variable "userQuestion")
- Form field with ID: customerName (stores in workflow variable "customerName") 
- Form field with ID: orderAmount (stores in workflow variable "orderAmount")
- Form field with ID: productType (stores in workflow variable "productType")

Plugin Configuration:
User Prompt Variable: userQuestion
Additional Context Variables: customerName,orderAmount,productType
Custom Prompt Template: 
  Analyze this customer request: #{variable.userQuestion}
  
  Customer Details:
  - Name: #{variable.customerName}
  - Order Amount: #{variable.orderAmount}
  - Product Type: #{variable.productType}
  
  Provide recommendations based on this context.
```

### Variable Validation Checklist

Before using the plugin, verify:
- ✅ All input variables exist in your workflow
- ✅ Form fields have correct IDs matching variable names
- ✅ Variables contain actual data (not empty)
- ✅ Variable names don't include # symbols in plugin configuration
- ✅ Process flow allows variables to be set before AI tool execution

## API Models

| Model | Description | Best For |
|-------|-------------|----------|
| `gemini-1.5-flash` | Fast responses, good quality | Quick processing, real-time applications |
| `gemini-1.5-pro` | Advanced reasoning | Complex analysis, detailed responses |
| `gemini-2.0-flash` | Latest fast model | Enhanced speed with improved quality |
| `gemini-2.0-pro` | Latest advanced model | Most sophisticated analysis and reasoning |

## Workflow Integration

### Input Variables
The plugin reads from workflow variables you specify:
- Primary prompt from `User Prompt Variable` 
- Additional context from `Additional Context Variables`
- Custom formatting via `Custom Prompt Template`

### Output Variables
The AI response is automatically stored in the specified `Output Variable Name` and can be used in:
- Subsequent workflow activities
- Form fields using `#{variable.aiResponse}`
- Decision nodes for conditional logic
- Email notifications and reports

## Error Handling

The plugin includes comprehensive error handling:
- **Missing API Key**: Clear error message with configuration guidance
- **Network Issues**: Timeout handling with retry logic
- **Invalid Responses**: JSON parsing error handling
- **Variable Issues**: Graceful handling of missing workflow variables

## Debug Mode

Enable debug logging by adding a `.env` file in `src/main/resources/`:
```
DEBUG=true
```

Debug logs include:
- Configuration validation
- API request/response details
- Variable processing information
- Error diagnostics

## Security Notes

- API keys are stored securely using Joget's encrypted password field type
- Network requests use HTTPS encryption
- No sensitive data is logged in production mode

## Troubleshooting

### Common Issues

**"API Key is required" error**
- Ensure your Gemini API key is properly configured in the plugin settings
- Verify the API key is valid and has Gemini API access enabled

**"No valid prompt could be constructed" error**
- Check that your `User Prompt Variable` contains data
- Verify variable names don't include `#` symbols in configuration
- Ensure the specified workflow variables exist

**Empty AI responses**
- Check your API key has sufficient quota
- Verify the Gemini model you selected is available
- Review debug logs for API error details

**Output variable not populated**
- Confirm the `Output Variable Name` is correctly specified
- Check that subsequent activities can access the variable
- Verify workflow variable scope and timing

## Support

For issues and questions:
1. Enable debug mode and check logs
2. Verify your Gemini API key and quota
3. Test with simple prompts first
4. Review Joget workflow variable configuration

## Version History

- **v1.0.0** - Initial release with Google Gemini AI integration

---

## License

This plugin is developed for use with Joget Workflow platform.