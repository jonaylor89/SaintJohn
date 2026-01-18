# Agent Skills System

The Saint John launcher features an agentic chat system that allows the AI assistant to perform actions on the device and retrieve information. This document outlines the architecture and how to add new capabilities.

## Architecture

The agent system is built on a few core components:

1.  **`AgentSkill`**: An abstract base class that defines a tool/skill. Each skill has a unique name, description, and a list of parameters.
2.  **`SkillRegistry`**: A singleton that holds all registered skills. It's injected into the `ChatRepository` and `ChatViewModel`.
3.  **`ChatRepository`**: Handles the communication with LLM providers (Gemini, Anthropic, OpenAI). It is responsible for:
    *   Converting `AgentSkill` definitions into the specific format required by each provider (e.g., `GeminiTool`, `AnthropicTool`).
    *   Parsing incoming messages to detect tool calls.
    *   Sending tool results back to the LLM.
4.  **`ChatViewModel`**: Manages the "Agentic Loop". It observes messages, executes tools when requested by the LLM, and triggers the next turn in the conversation.

### The Agentic Loop

1.  **User sends a message**: "What's the weather in London?"
2.  **LLM responds**: Instead of text, it returns a **Tool Call** (e.g., `get_weather(location="London")`).
3.  **ViewModel detects Tool Call**: It looks up the skill in `SkillRegistry` and executes it.
4.  **Tool Execution**: The skill runs (e.g., fetches weather data) and returns a string result.
5.  **Result Sent**: The result is sent back to the LLM as a new message with role `TOOL`.
6.  **LLM Final Response**: The LLM processes the tool result and generates a natural language response (e.g., "The weather in London is 15°C...").

## Implemented Skills

The following skills are currently available:

| Skill Name | Description | Parameters |
| :--- | :--- | :--- |
| `launch_app` | Launches an application by name or package name. | `app_name` (string), `package_name` (string) |
| `list_apps` | Lists all installed applications on the device. | None |
| `get_weather` | Gets current weather for a location. | `location` (string, optional) |
| `create_note` | Creates a new note in the launcher. | `title` (string), `content` (string) |
| `get_calendar_events` | Gets upcoming calendar events. | `limit` (int, optional) |

## Adding a New Skill

To add a new skill to the agent, follow these steps:

### 1. Create the Skill Class

Create a new class in `app/src/main/java/com/jonaylor/saintjohn/domain/agent/skills/` that extends `AgentSkill`.

```kotlin
package com.jonaylor.saintjohn.domain.agent.skills

import com.jonaylor.saintjohn.domain.agent.AgentSkill
import com.jonaylor.saintjohn.domain.agent.SkillParameter
import javax.inject.Inject

class MyNewSkill @Inject constructor(
    // Inject any repositories or managers you need
    private val myRepository: MyRepository
) : AgentSkill(
    name = "my_new_skill",
    description = "Description of what this skill does.",
    params = listOf(
        SkillParameter(
            name = "param_name",
            type = "string", // "string", "integer", "boolean", "number"
            description = "Description of the parameter",
            required = true
        )
    )
) {
    override suspend fun execute(args: Map<String, Any?>): String {
        val param = args["param_name"] as? String
        
        // Perform your action here
        val result = myRepository.doSomething(param)
        
        return "Action completed successfully: $result"
    }
}
```

### 2. Register the Skill

Add your skill to the `AgentModule` in `app/src/main/java/com/jonaylor/saintjohn/di/AgentModule.kt`.

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AgentModule {
    // ... existing skills ...

    @Provides
    @IntoSet
    fun provideMyNewSkill(skill: MyNewSkill): AgentSkill {
        return skill
    }
}
```

That's it! The `SkillRegistry` will automatically pick up your new skill, and the `ChatRepository` will expose it to the LLMs.

## Provider Support

*   **Google (Gemini)**: Fully supported using Gemini Function Calling.
*   **Anthropic (Claude)**: Fully supported using Claude Tool Use.
*   **OpenAI**: Fully supported using OpenAI Tool Calling.
