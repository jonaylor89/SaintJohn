package com.jonaylor.saintjohn.data.repository

import com.jonaylor.saintjohn.data.local.PreferencesManager
import com.jonaylor.saintjohn.data.local.dao.ConversationDao
import com.jonaylor.saintjohn.data.local.dao.MessageDao
import com.jonaylor.saintjohn.data.local.entity.ConversationEntity
import com.jonaylor.saintjohn.data.local.entity.MessageEntity
import com.google.gson.Gson
import com.jonaylor.saintjohn.data.remote.AnthropicApi
import com.jonaylor.saintjohn.data.remote.GeminiApi
import com.jonaylor.saintjohn.data.remote.OpenAIApi
import com.jonaylor.saintjohn.data.remote.dto.AnthropicMessage
import com.jonaylor.saintjohn.data.remote.dto.AnthropicRequest
import com.jonaylor.saintjohn.data.remote.dto.AnthropicStreamChunk
import com.jonaylor.saintjohn.data.remote.dto.AnthropicTool
import com.jonaylor.saintjohn.data.remote.dto.AnthropicSchema
import com.jonaylor.saintjohn.data.remote.dto.AnthropicSchemaProperty
import com.jonaylor.saintjohn.data.remote.dto.AnthropicContent
import com.jonaylor.saintjohn.data.remote.dto.GeminiContent
import com.jonaylor.saintjohn.data.remote.dto.OpenAITool
import com.jonaylor.saintjohn.data.remote.dto.OpenAIFunction
import com.jonaylor.saintjohn.data.remote.dto.OpenAISchema
import com.jonaylor.saintjohn.data.remote.dto.OpenAISchemaProperty
import com.jonaylor.saintjohn.data.remote.dto.OpenAIToolCall
import com.jonaylor.saintjohn.data.remote.dto.OpenAIFunctionCall
import com.jonaylor.saintjohn.data.remote.dto.GeminiGenerationConfig
import com.jonaylor.saintjohn.data.remote.dto.GeminiPart
import com.jonaylor.saintjohn.data.remote.dto.GeminiRequest
import com.jonaylor.saintjohn.data.remote.dto.GeminiStreamChunk
import com.jonaylor.saintjohn.data.remote.dto.GeminiFunctionCall
import com.jonaylor.saintjohn.data.remote.dto.GeminiFunctionResponse
import com.jonaylor.saintjohn.data.remote.dto.OpenAIMessage
import com.jonaylor.saintjohn.data.remote.dto.OpenAIRequest
import com.jonaylor.saintjohn.data.remote.dto.OpenAIStreamChunk
import com.jonaylor.saintjohn.domain.model.LLMProvider
import com.jonaylor.saintjohn.domain.model.Message
import com.jonaylor.saintjohn.domain.model.MessageImage
import com.jonaylor.saintjohn.domain.model.MessageRole
import com.google.gson.reflect.TypeToken
import com.jonaylor.saintjohn.domain.model.ToolResult
import com.jonaylor.saintjohn.domain.model.MessageSource
import com.jonaylor.saintjohn.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

import com.jonaylor.saintjohn.domain.agent.SkillRegistry
import com.jonaylor.saintjohn.domain.agent.AgentSkill
import com.jonaylor.saintjohn.data.remote.dto.GeminiTool
import com.jonaylor.saintjohn.data.remote.dto.GeminiFunctionDeclaration
import com.jonaylor.saintjohn.data.remote.dto.GeminiSchema
import com.jonaylor.saintjohn.data.remote.dto.GeminiSchemaProperty
import com.jonaylor.saintjohn.domain.model.ToolCall

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao,
    private val preferencesManager: PreferencesManager,
    private val openAIApi: OpenAIApi,
    private val geminiApi: GeminiApi,
    private val anthropicApi: AnthropicApi,
    private val skillRegistry: SkillRegistry
) : ChatRepository {

    private var currentConversationId: Long? = null

    override fun getMessages(conversationId: Long): Flow<List<Message>> {
        return messageDao.getMessagesByConversation(conversationId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getAllConversations(): Flow<List<ConversationEntity>> {
        return conversationDao.getAllConversations()
    }

    override suspend fun getMessageCount(conversationId: Long): Int {
        return messageDao.getMessageCount(conversationId)
    }

    override suspend fun switchToConversation(conversationId: Long) {
        currentConversationId = conversationId
    }

    override suspend fun deleteConversation(conversation: ConversationEntity) {
        messageDao.deleteMessagesByConversation(conversation.id)
        conversationDao.deleteConversation(conversation)
    }

    override suspend fun deleteEmptyAssistantMessages(conversationId: Long) {
        messageDao.deleteEmptyAssistantMessages(conversationId)
    }

    override suspend fun sendMessage(
        conversationId: Long,
        content: String?,
        provider: LLMProvider
    ): Result<Message> {
        return try {
            // Save user message
            if (content != null) {
                val userMessage = MessageEntity(
                    conversationId = conversationId,
                    content = content,
                    role = MessageRole.USER.name,
                    timestamp = System.currentTimeMillis()
                )
                messageDao.insertMessage(userMessage)
            }

            // Get API key
            val apiKey = when (provider) {
                LLMProvider.OPENAI -> preferencesManager.openaiApiKey.first()
                LLMProvider.ANTHROPIC -> preferencesManager.anthropicApiKey.first()
                LLMProvider.GOOGLE -> preferencesManager.googleApiKey.first()
            }

            if (apiKey.isBlank()) {
                val errorMessage = MessageEntity(
                    conversationId = conversationId,
                    content = "Please set your API key in settings first.",
                    role = MessageRole.ASSISTANT.name,
                    timestamp = System.currentTimeMillis(),
                    isError = true
                )
                val id = messageDao.insertMessage(errorMessage)
                return Result.success(errorMessage.copy(id = id).toDomainModel())
            }

            // Make API call based on provider
            val assistantContent = when (provider) {
                LLMProvider.OPENAI -> {
                    // Get conversation history for context
                    val history = messageDao.getMessagesByConversation(conversationId).first()

                    // Convert to OpenAI format
                    val openAIMessages = history.map { msg ->
                        OpenAIMessage(
                            role = msg.role.lowercase(),
                            content = msg.content
                        )
                    }

                    // Get selected model for this provider
                    val selectedModel = getSelectedModel(provider)

                    // Make API call
                    val request = OpenAIRequest(
                        model = selectedModel,
                        messages = openAIMessages,
                        stream = false
                    )

                    val response = openAIApi.createChatCompletion(
                        authorization = "Bearer $apiKey",
                        request = request
                    )

                    // Extract response content
                    response.choices.firstOrNull()?.message?.content
                        ?: throw Exception("No response from OpenAI")
                }

                LLMProvider.ANTHROPIC -> {
                    // Get conversation history for context
                    val history = messageDao.getMessagesByConversation(conversationId).first()

                    // Convert to Anthropic format
                    val anthropicMessages = history.map { msg ->
                        AnthropicMessage(
                            role = msg.role.lowercase(),
                            content = msg.content
                        )
                    }

                    // Get selected model for this provider
                    val selectedModel = getSelectedModel(provider)

                    // Make API call
                    val request = AnthropicRequest(
                        model = selectedModel,
                        messages = anthropicMessages,
                        stream = false
                    )

                    val response = anthropicApi.createMessage(
                        apiKey = apiKey,
                        request = request
                    )

                    // Extract response content
                    response.content.firstOrNull()?.text
                        ?: throw Exception("No response from Anthropic")
                }

                LLMProvider.GOOGLE -> {
                    // Get conversation history for context
                    val history = messageDao.getMessagesByConversation(conversationId).first()

                    // Convert to Gemini format
                    val geminiContents = history.map { msg ->
                        GeminiContent(
                            parts = listOf(GeminiPart(text = msg.content)),
                            role = if (msg.role == "USER") "user" else "model"
                        )
                    }

                    // Get selected model for this provider
                    val selectedModel = getSelectedModel(provider)

                    // Make API call
                    val request = GeminiRequest(contents = geminiContents)

                    val response = geminiApi.generateContent(
                        model = selectedModel,
                        apiKey = apiKey,
                        request = request
                    )

                    // Extract response content
                    response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        ?: throw Exception("No response from Gemini")
                }
            }

            // Save assistant response
            val assistantMessage = MessageEntity(
                conversationId = conversationId,
                content = assistantContent,
                role = MessageRole.ASSISTANT.name,
                timestamp = System.currentTimeMillis()
            )
            val id = messageDao.insertMessage(assistantMessage)

            Result.success(assistantMessage.copy(id = id).toDomainModel())
        } catch (e: Exception) {
            val is404 = (e is retrofit2.HttpException && e.code() == 404) || 
                        e.message?.contains("404") == true
            
            val errorMessageText = if (is404) {
                "Error: 404 Not Found. This usually means the model ID is invalid or not available for your account. Model: ${getSelectedModel(provider)}"
            } else {
                "Error: ${e.message ?: "Unknown error occurred"}"
            }
            
            // Save error message for user to see
            val errorMessage = MessageEntity(
                conversationId = conversationId,
                content = errorMessageText,
                role = MessageRole.ASSISTANT.name,
                timestamp = System.currentTimeMillis(),
                isError = true
            )
            val id = messageDao.insertMessage(errorMessage)

            Result.failure(e)
        }
    }

    override suspend fun sendMessageStreaming(
        conversationId: Long,
        content: String?,
        provider: LLMProvider,
        onChunk: (String) -> Unit,
        sources: List<MessageSource>
    ): Result<Message> {
        return try {
            // Save user message
            if (content != null) {
                val userMessage = MessageEntity(
                    conversationId = conversationId,
                    content = content,
                    role = MessageRole.USER.name,
                    timestamp = System.currentTimeMillis()
                )
                messageDao.insertMessage(userMessage)

                // Update conversation title if this is the first user message
                val messageCount = messageDao.getMessageCount(conversationId)
                if (messageCount == 1) { // Only the user message we just inserted
                    val preview = if (content.length > 20) {
                        content.take(20).trim() + "..."
                    } else {
                        content.trim()
                    }
                    conversationDao.updateConversationTitle(conversationId, preview)
                }
            }

            // Get API key
            val apiKey = when (provider) {
                LLMProvider.OPENAI -> preferencesManager.openaiApiKey.first()
                LLMProvider.ANTHROPIC -> preferencesManager.anthropicApiKey.first()
                LLMProvider.GOOGLE -> preferencesManager.googleApiKey.first()
            }

            if (apiKey.isBlank()) {
                val errorMessage = MessageEntity(
                    conversationId = conversationId,
                    content = "Please set your API key in settings first.",
                    role = MessageRole.ASSISTANT.name,
                    timestamp = System.currentTimeMillis(),
                    isError = true
                )
                val id = messageDao.insertMessage(errorMessage)
                return Result.success(errorMessage.copy(id = id).toDomainModel())
            }

            // Create placeholder assistant message
            val assistantMessage = MessageEntity(
                conversationId = conversationId,
                content = "",
                role = MessageRole.ASSISTANT.name,
                timestamp = System.currentTimeMillis()
            )
            val messageId = messageDao.insertMessage(assistantMessage)

            // Get system prompt
            val systemPrompt = preferencesManager.systemPrompt.first()

            // Make streaming API call
            val assistantContent = when (provider) {
                LLMProvider.OPENAI -> {
                    // Get conversation history for context
                    val history = messageDao.getMessagesByConversation(conversationId).first()
                        .filter { it.id != messageId } // Exclude the placeholder

                    // Convert to OpenAI format with optional system prompt
                    val openAIMessages = buildList {
                        if (systemPrompt.isNotBlank()) {
                            add(OpenAIMessage(role = "system", content = systemPrompt))
                        }
                        addAll(history.map { msg ->
                            val role = if (msg.role == MessageRole.TOOL.name) "tool" else msg.role.lowercase()
                            
                            val toolCalls = if (msg.toolCallsJson != null) {
                                try {
                                    val type = object : TypeToken<List<ToolCall>>() {}.type
                                    val tcs = Gson().fromJson<List<ToolCall>>(msg.toolCallsJson, type)
                                    tcs.map { tc ->
                                        OpenAIToolCall(
                                            id = tc.id,
                                            function = OpenAIFunctionCall(
                                                name = tc.name,
                                                arguments = Gson().toJson(tc.arguments)
                                            )
                                        )
                                    }
                                } catch (e: Exception) { null }
                            } else null
                            
                            val toolCallId = if (msg.role == MessageRole.TOOL.name && msg.toolResultJson != null) {
                                try {
                                    Gson().fromJson(msg.toolResultJson, ToolResult::class.java).toolCallId
                                } catch (e: Exception) { null }
                            } else null

                            OpenAIMessage(
                                role = role,
                                content = msg.content,
                                toolCalls = toolCalls,
                                toolCallId = toolCallId
                            )
                        })
                    }

                    // Get selected model for this provider
                    val selectedModel = getSelectedModel(provider)

                    // Prepare tools
                    val skills = skillRegistry.getAllSkills()
                    val openAITools = if (skills.isNotEmpty()) {
                        skills.map { it.toOpenAITool() }
                    } else null

                    // Make streaming API call
                    val request = OpenAIRequest(
                        model = selectedModel,
                        messages = openAIMessages,
                        stream = true,
                        tools = openAITools
                    )

                    val responseBody = openAIApi.createChatCompletionStream(
                        authorization = "Bearer $apiKey",
                        request = request
                    )

                    val fullContent = StringBuilder()
                    val fullThinking = StringBuilder()
                    
                    // Track tool calls being built from stream
                    // Map of index -> ToolCallBuilder
                    val toolCallBuilders = mutableMapOf<Int, MutableToolCallBuilder>()
                    
                    val gson = Gson()

                    // Process stream line by line
                    responseBody.byteStream().bufferedReader().use { reader ->
                        reader.lineSequence()
                            .filter { it.startsWith("data: ") }
                            .forEach { line ->
                                val data = line.substring(6) // Remove "data: " prefix
                                if (data == "[DONE]") return@forEach

                                try {
                                    val chunk = gson.fromJson(data, OpenAIStreamChunk::class.java)
                                    val delta = chunk.choices.firstOrNull()?.delta
                                    
                                    // Handle reasoning content (for o1, o3 models)
                                    delta?.reasoningContent?.let { reasoning ->
                                        fullThinking.append(reasoning)
                                        messageDao.updateMessage(
                                            assistantMessage.copy(
                                                id = messageId,
                                                content = fullContent.toString(),
                                                thinking = fullThinking.toString()
                                            )
                                        )
                                    }
                                    
                                    // Handle regular content
                                    delta?.content?.let { content ->
                                        fullContent.append(content)
                                        messageDao.updateMessage(
                                            assistantMessage.copy(
                                                id = messageId,
                                                content = fullContent.toString(),
                                                thinking = fullThinking.toString().takeIf { it.isNotEmpty() }
                                            )
                                        )
                                        onChunk(content)
                                    }
                                    
                                    // Handle tool calls
                                    delta?.toolCalls?.forEach { toolCallChunk ->
                                        val index = toolCallChunk.index
                                        val builder = toolCallBuilders.getOrPut(index) { MutableToolCallBuilder() }
                                        
                                        toolCallChunk.id?.let { builder.id = it }
                                        toolCallChunk.function?.name?.let { builder.name = it }
                                        toolCallChunk.function?.arguments?.let { builder.argumentsBuilder.append(it) }
                                    }
                                    
                                    // If we have accumulated tool calls, update the DB
                                    if (toolCallBuilders.isNotEmpty()) {
                                        val currentToolCalls = toolCallBuilders.values
                                            .filter { it.id != null && it.name != null }
                                            .map { builder ->
                                                // Try to parse partial arguments JSON
                                                val argsMap = try {
                                                    val type = object : TypeToken<Map<String, Any?>>() {}.type
                                                    gson.fromJson<Map<String, Any?>>(builder.argumentsBuilder.toString(), type) ?: emptyMap()
                                                } catch (e: Exception) { emptyMap() }
                                                
                                                ToolCall(
                                                    id = builder.id!!,
                                                    name = builder.name!!,
                                                    arguments = argsMap
                                                )
                                            }
                                            
                                        if (currentToolCalls.isNotEmpty()) {
                                            messageDao.updateMessage(
                                                assistantMessage.copy(
                                                    id = messageId,
                                                    content = fullContent.toString(),
                                                    thinking = fullThinking.toString().takeIf { it.isNotEmpty() },
                                                    toolCallsJson = gson.toJson(currentToolCalls)
                                                )
                                            )
                                        }
                                    }
                                } catch (e: Exception) {
                                    // Skip malformed chunks
                                }
                            }
                    }

                    // Store final thinking content and tool calls
                    val finalToolCalls = toolCallBuilders.values
                        .filter { it.id != null && it.name != null }
                        .map { builder ->
                            val argsMap = try {
                                val type = object : TypeToken<Map<String, Any?>>() {}.type
                                gson.fromJson<Map<String, Any?>>(builder.argumentsBuilder.toString(), type) ?: emptyMap()
                            } catch (e: Exception) { emptyMap() }
                            
                            ToolCall(
                                id = builder.id!!,
                                name = builder.name!!,
                                arguments = argsMap
                            )
                        }

                    if (fullThinking.isNotEmpty() || finalToolCalls.isNotEmpty()) {
                        messageDao.updateMessage(
                            assistantMessage.copy(
                                id = messageId,
                                content = fullContent.toString(),
                                thinking = fullThinking.toString(),
                                toolCallsJson = if (finalToolCalls.isNotEmpty()) Gson().toJson(finalToolCalls) else null
                            )
                        )
                    }

                    fullContent.toString()
                }

                LLMProvider.ANTHROPIC -> {
                    // Get conversation history for context
                    val history = messageDao.getMessagesByConversation(conversationId).first()
                        .filter { it.id != messageId } // Exclude the placeholder

                    // Convert to Anthropic format
                    val anthropicMessages = history.map { msg ->
                        val content: Any = when {
                            msg.toolCallsJson != null -> {
                                val type = object : TypeToken<List<ToolCall>>() {}.type
                                val toolCalls = Gson().fromJson<List<ToolCall>>(msg.toolCallsJson, type)
                                buildList {
                                    if (msg.content.isNotEmpty()) {
                                        add(AnthropicContent(type = "text", text = msg.content))
                                    }
                                    toolCalls.forEach { tc ->
                                        add(AnthropicContent(type = "tool_use", id = tc.id, name = tc.name, input = tc.arguments))
                                    }
                                }
                            }
                            msg.toolResultJson != null -> {
                                val toolResult = Gson().fromJson(msg.toolResultJson, ToolResult::class.java)
                                listOf(AnthropicContent(type = "tool_result", toolUseId = toolResult.toolCallId, content = toolResult.result))
                            }
                            else -> msg.content
                        }
                        
                        AnthropicMessage(
                            role = if (msg.role == MessageRole.TOOL.name) "user" else msg.role.lowercase(),
                            content = content
                        )
                    }

                    // Get selected model for this provider
                    val selectedModel = getSelectedModel(provider)

                    // Prepare tools
                    val skills = skillRegistry.getAllSkills()
                    val anthropicTools = if (skills.isNotEmpty()) {
                        skills.map { it.toAnthropicTool() }
                    } else null

                    // Make streaming API call with system prompt
                    val request = AnthropicRequest(
                        model = selectedModel,
                        messages = anthropicMessages,
                        stream = true,
                        system = systemPrompt.takeIf { it.isNotBlank() },
                        tools = anthropicTools
                    )

                    val responseBody = anthropicApi.createMessageStream(
                        apiKey = apiKey,
                        request = request
                    )

                    val fullContent = StringBuilder()
                    val fullThinking = StringBuilder()
                    val collectedToolCalls = mutableListOf<ToolCall>()
                    var currentToolCallId: String? = null
                    var currentToolCallName: String? = null
                    val currentToolCallInput = StringBuilder()
                    
                    val gson = Gson()
                    var currentBlockType: String? = null

                    // Process stream line by line
                    responseBody.byteStream().bufferedReader().use { reader ->
                        reader.lineSequence()
                            .forEach { line ->
                                android.util.Log.d("AnthropicStream", "Raw line: $line")

                                // Anthropic SSE format: lines starting with "data: "
                                if (line.startsWith("data: ")) {
                                    val data = line.substring(6) // Remove "data: " prefix

                                    try {
                                        val chunk = gson.fromJson(data, AnthropicStreamChunk::class.java)

                                        // Handle different chunk types
                                        when (chunk.type) {
                                            "content_block_start" -> {
                                                // Track the type of the current block (thinking vs text vs tool_use)
                                                currentBlockType = chunk.contentBlock?.type
                                                if (currentBlockType == "tool_use") {
                                                    currentToolCallId = chunk.contentBlock?.id
                                                    currentToolCallName = chunk.contentBlock?.name
                                                    currentToolCallInput.setLength(0)
                                                }
                                            }
                                            "content_block_delta" -> {
                                                val deltaType = chunk.delta?.type
                                                
                                                when {
                                                    deltaType == "thinking_delta" || currentBlockType == "thinking" -> {
                                                        chunk.delta?.thinking?.let { thinking ->
                                                            fullThinking.append(thinking)
                                                            messageDao.updateMessage(
                                                                assistantMessage.copy(
                                                                    id = messageId,
                                                                    content = fullContent.toString(),
                                                                    thinking = fullThinking.toString()
                                                                )
                                                            )
                                                        }
                                                    }
                                                    deltaType == "text_delta" || currentBlockType == "text" -> {
                                                        chunk.delta?.text?.let { content ->
                                                            fullContent.append(content)
                                                            messageDao.updateMessage(
                                                                assistantMessage.copy(
                                                                    id = messageId,
                                                                    content = fullContent.toString(),
                                                                    thinking = fullThinking.toString().takeIf { it.isNotEmpty() }
                                                                )
                                                            )
                                                            onChunk(content)
                                                        }
                                                    }
                                                    deltaType == "input_json_delta" -> {
                                                        chunk.delta?.partialJson?.let { partial ->
                                                            currentToolCallInput.append(partial)
                                                        }
                                                    }
                                                }
                                            }
                                            "content_block_stop" -> {
                                                if (currentBlockType == "tool_use" && currentToolCallId != null && currentToolCallName != null) {
                                                    try {
                                                        val inputMapType = object : TypeToken<Map<String, Any?>>() {}.type
                                                        val arguments = gson.fromJson<Map<String, Any?>>(currentToolCallInput.toString(), inputMapType)
                                                        collectedToolCalls.add(
                                                            ToolCall(
                                                                id = currentToolCallId!!,
                                                                name = currentToolCallName!!,
                                                                arguments = arguments
                                                            )
                                                        )
                                                        
                                                        // Update database with collected tool calls
                                                        messageDao.updateMessage(
                                                            assistantMessage.copy(
                                                                id = messageId,
                                                                content = fullContent.toString(),
                                                                thinking = fullThinking.toString().takeIf { it.isNotEmpty() },
                                                                toolCallsJson = Gson().toJson(collectedToolCalls)
                                                            )
                                                        )
                                                    } catch (e: Exception) {
                                                        android.util.Log.e("AnthropicStream", "Error parsing tool input", e)
                                                    }
                                                }
                                                currentBlockType = null
                                            }
                                            "message_stop" -> {
                                                // End of stream
                                                return@forEach
                                            }
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("AnthropicStream", "Error parsing chunk: $data", e)
                                    }
                                }
                            }
                    }

                    // Store final thinking content
                    if (fullThinking.isNotEmpty() || collectedToolCalls.isNotEmpty()) {
                        messageDao.updateMessage(
                            assistantMessage.copy(
                                id = messageId,
                                content = fullContent.toString(),
                                thinking = fullThinking.toString().takeIf { it.isNotEmpty() },
                                toolCallsJson = if (collectedToolCalls.isNotEmpty()) Gson().toJson(collectedToolCalls) else null
                            )
                        )
                    }

                    fullContent.toString()
                }

                LLMProvider.GOOGLE -> {
                    // Get conversation history for context
                    val history = messageDao.getMessagesByConversation(conversationId).first()
                        .filter { it.id != messageId } // Exclude the placeholder

                    // Convert to Gemini format
                    val geminiContents = history.map { msg ->
                        val parts = mutableListOf<GeminiPart>()
                        
                        // Content
                        if (msg.content.isNotEmpty()) {
                            parts.add(GeminiPart(text = msg.content))
                        }
                        
                        // Tool Calls
                        if (msg.toolCallsJson != null) {
                            try {
                                val type = object : TypeToken<List<ToolCall>>() {}.type
                                val toolCalls = Gson().fromJson<List<ToolCall>>(msg.toolCallsJson, type)
                                toolCalls.forEach { tc ->
                                    parts.add(GeminiPart(functionCall = GeminiFunctionCall(tc.name, tc.arguments)))
                                }
                            } catch (e: Exception) { }
                        }
                        
                        // Tool Results
                        if (msg.toolResultJson != null) {
                            try {
                                val toolResult = Gson().fromJson(msg.toolResultJson, ToolResult::class.java)
                                val responseMap = mapOf("result" to toolResult.result) 
                                parts.add(GeminiPart(functionResponse = GeminiFunctionResponse(toolResult.toolCallId, responseMap)))
                            } catch (e: Exception) { }
                        }

                        // Determine role
                        val role = if (msg.role == MessageRole.USER.name || msg.role == MessageRole.TOOL.name) "user" else "model"
                        
                        GeminiContent(parts = parts, role = role)
                    }

                    // Get selected model for this provider
                    val selectedModel = getSelectedModel(provider)

                    // Make streaming API call with system instruction
                    val systemInstruction = if (systemPrompt.isNotBlank()) {
                        GeminiContent(parts = listOf(GeminiPart(text = systemPrompt)))
                    } else null

                    // For image generation models, include Image in response modalities
                    val isImageModel = selectedModel.contains("image") || 
                                       selectedModel.contains("nano-banana") ||
                                       selectedModel == "gemini-2.5-flash-preview-native-audio-dialog"
                    val generationConfig = if (isImageModel) {
                        GeminiGenerationConfig(responseModalities = listOf("TEXT", "IMAGE"))
                    } else null

                    // Prepare tools
                    val skills = skillRegistry.getAllSkills()
                    val geminiTools = if (skills.isNotEmpty()) {
                        listOf(GeminiTool(functionDeclarations = skills.map { it.toGeminiFunction() }))
                    } else null

                    val request = GeminiRequest(
                        contents = geminiContents,
                        systemInstruction = systemInstruction,
                        generationConfig = generationConfig,
                        tools = geminiTools
                    )

                    val responseBody = geminiApi.streamGenerateContent(
                        model = selectedModel,
                        apiKey = apiKey,
                        request = request
                    )

                    val fullContent = StringBuilder()
                    val collectedImages = mutableListOf<MessageImage>()
                    val collectedToolCalls = mutableListOf<ToolCall>()
                    val gson = Gson()

                    // Process stream line by line
                    responseBody.byteStream().bufferedReader().use { reader ->
                        reader.lineSequence()
                            .forEach { line ->
                                android.util.Log.d("GeminiStream", "Raw line: $line")

                                // Gemini SSE format: lines starting with "data: "
                                if (line.startsWith("data: ")) {
                                    val data = line.substring(6) // Remove "data: " prefix
                                    if (data == "[DONE]") return@forEach

                                    try {
                                        val chunk = gson.fromJson(data, GeminiStreamChunk::class.java)
                                        chunk.candidates?.firstOrNull()?.content?.parts?.forEach { part ->
                                            // Handle text parts
                                            part.text?.let { content ->
                                                fullContent.append(content)
                                                onChunk(content)
                                            }
                                            
                                            // Handle image parts (Nano Banana image generation)
                                            part.inlineData?.let { inlineData ->
                                                if (inlineData.mimeType.startsWith("image/")) {
                                                    collectedImages.add(
                                                        MessageImage(
                                                            data = inlineData.data,
                                                            mimeType = inlineData.mimeType
                                                        )
                                                    )
                                                    android.util.Log.d("GeminiStream", "Received image: ${inlineData.mimeType}")
                                                }
                                            }

                                            // Handle function calls
                                            part.functionCall?.let { fc ->
                                                collectedToolCalls.add(
                                                    ToolCall(
                                                        id = "call_${System.currentTimeMillis()}_${collectedToolCalls.size}",
                                                        name = fc.name,
                                                        arguments = fc.args
                                                    )
                                                )
                                            }
                                        }

                                        // Update the message in database in real-time
                                        messageDao.updateMessage(
                                            assistantMessage.copy(
                                                id = messageId,
                                                content = fullContent.toString(),
                                                imagesJson = collectedImages.toJson(),
                                                toolCallsJson = if (collectedToolCalls.isNotEmpty()) Gson().toJson(collectedToolCalls) else null
                                            )
                                        )
                                    } catch (e: Exception) {
                                        android.util.Log.e("GeminiStream", "Error parsing chunk: $data", e)
                                    }
                                }
                            }
                    }

                    fullContent.toString()
                }
            }

            // Update message with sources if provided
            if (sources.isNotEmpty()) {
                val currentMessage = messageDao.getMessageById(messageId)
                if (currentMessage != null) {
                    messageDao.updateMessage(
                        currentMessage.copy(sourcesJson = Gson().toJson(sources))
                    )
                }
            }
            
            // Message was already inserted and updated during streaming
            // Just return the final version
            val finalMessage = messageDao.getMessageById(messageId) ?: assistantMessage.copy(id = messageId, content = assistantContent)
            Result.success(finalMessage.toDomainModel())
        } catch (e: Exception) {
            val is404 = (e is retrofit2.HttpException && e.code() == 404) || 
                        e.message?.contains("404") == true

            val errorMessageText = if (is404) {
                "Error: 404 Not Found. This usually means the model ID is invalid or not available for your account. Model: ${getSelectedModel(provider)}"
            } else {
                "Error: ${e.message ?: "Unknown error occurred"}"
            }

            // Save error message for user to see
            val errorMessage = MessageEntity(
                conversationId = conversationId,
                content = errorMessageText,
                role = MessageRole.ASSISTANT.name,
                timestamp = System.currentTimeMillis(),
                isError = true
            )
            val id = messageDao.insertMessage(errorMessage)

            Result.failure(e)
        }
    }

    override suspend fun createNewConversation(provider: LLMProvider, model: String): Long {
        val conversation = ConversationEntity(
            title = "New Chat",
            provider = provider.name,
            model = model,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val id = conversationDao.insertConversation(conversation)
        currentConversationId = id
        return id
    }

    override suspend fun getMostRecentConversationId(): Long? {
        return conversationDao.getMostRecentConversation()?.id
    }

    override suspend fun getAvailableModels(provider: LLMProvider): Result<List<String>> {
        return try {
            val apiKey = when (provider) {
                LLMProvider.OPENAI -> preferencesManager.openaiApiKey.first()
                LLMProvider.ANTHROPIC -> preferencesManager.anthropicApiKey.first()
                LLMProvider.GOOGLE -> preferencesManager.googleApiKey.first()
            }

            // If no API key, return fallback static list
            if (apiKey.isBlank()) {
                return Result.success(getFallbackModels(provider))
            }

            when (provider) {
                LLMProvider.OPENAI -> {
                    // Fetch models dynamically from OpenAI API
                    val response = openAIApi.listModels("Bearer $apiKey")
                    val chatModels = response.data
                        .map { it.id }
                        .filter { id ->
                            // Filter to chat-capable models only
                            id.startsWith("gpt-") || 
                            id.startsWith("o1") || 
                            id.startsWith("o3") ||
                            id.startsWith("o4")
                        }
                        .filterNot { id ->
                            // Exclude embedding, audio, and internal models
                            id.contains("embedding") ||
                            id.contains("whisper") ||
                            id.contains("tts") ||
                            id.contains("dall-e") ||
                            id.contains("realtime") ||
                            id.contains("transcription") ||
                            id.contains("audio")
                        }
                        .sortedByDescending { id ->
                            // Sort by model family priority
                            when {
                                id.startsWith("gpt-5") -> 5
                                id.startsWith("o3") || id.startsWith("o4") -> 4
                                id.startsWith("gpt-4") -> 3
                                id.startsWith("o1") -> 2
                                else -> 1
                            }
                        }
                    
                    if (chatModels.isEmpty()) {
                        Result.success(getFallbackModels(provider))
                    } else {
                        Result.success(chatModels)
                    }
                }

                LLMProvider.ANTHROPIC -> {
                    // Fetch models dynamically from Anthropic API
                    val response = anthropicApi.listModels(apiKey)
                    val models = response.data
                        .map { it.id }
                        .filter { id ->
                            // Only include Claude chat models
                            id.startsWith("claude-")
                        }
                        .sortedByDescending { id ->
                            // Sort by model tier priority
                            when {
                                id.contains("opus") -> 3
                                id.contains("sonnet") -> 2
                                id.contains("haiku") -> 1
                                else -> 0
                            }
                        }
                    
                    if (models.isEmpty()) {
                        Result.success(getFallbackModels(provider))
                    } else {
                        Result.success(models)
                    }
                }

                LLMProvider.GOOGLE -> {
                    // Fetch models dynamically from Gemini API
                    val response = geminiApi.listModels(apiKey)
                    val chatModels = response.models
                        .filter { model ->
                            // Only include models that support generateContent
                            model.supportedGenerationMethods?.contains("generateContent") == true
                        }
                        .map { model ->
                            // Extract model ID from "models/gemini-xxx" format
                            model.name.removePrefix("models/")
                        }
                        .filter { id ->
                            // Only include Gemini models (not legacy PaLM)
                            id.startsWith("gemini-")
                        }
                        .sortedByDescending { id ->
                            // Sort by model generation and tier
                            when {
                                id.contains("3") && id.contains("pro") -> 6
                                id.contains("3") && id.contains("flash") -> 5
                                id.contains("2.5") && id.contains("pro") -> 4
                                id.contains("2.5") && id.contains("flash") -> 3
                                id.contains("2.0") -> 2
                                else -> 1
                            }
                        }
                    
                    if (chatModels.isEmpty()) {
                        Result.success(getFallbackModels(provider))
                    } else {
                        Result.success(chatModels)
                    }
                }
            }
        } catch (e: Exception) {
            // On any error, fall back to static list
            android.util.Log.e("ChatRepository", "Failed to fetch models dynamically", e)
            Result.success(getFallbackModels(provider))
        }
    }

    private fun getFallbackModels(provider: LLMProvider): List<String> {
        return when (provider) {
            LLMProvider.OPENAI -> listOf(
                "gpt-4o",
                "gpt-4o-mini",
                "gpt-4-turbo",
                "o1",
                "o1-mini"
            )
            LLMProvider.ANTHROPIC -> listOf(
                "claude-3-5-sonnet-20241022",
                "claude-3-5-haiku-20241022",
                "claude-3-opus-20240229"
            )
            LLMProvider.GOOGLE -> listOf(
                "gemini-1.5-pro",
                "gemini-1.5-flash",
                "gemini-1.0-pro"
            )
        }
    }

    override suspend fun getSelectedModel(provider: LLMProvider): String {
        return when (provider) {
            LLMProvider.OPENAI -> preferencesManager.selectedOpenAIModel.first()
            LLMProvider.ANTHROPIC -> preferencesManager.selectedAnthropicModel.first()
            LLMProvider.GOOGLE -> preferencesManager.selectedGoogleModel.first()
        }
    }

    override suspend fun setSelectedModel(provider: LLMProvider, model: String) {
        when (provider) {
            LLMProvider.OPENAI -> preferencesManager.setSelectedOpenAIModel(model)
            LLMProvider.ANTHROPIC -> preferencesManager.setSelectedAnthropicModel(model)
            LLMProvider.GOOGLE -> preferencesManager.setSelectedGoogleModel(model)
        }
    }

    override suspend fun sendToolResult(
        conversationId: Long,
        toolCallId: String,
        result: String
    ): Result<Message> {
        val toolMessage = MessageEntity(
            conversationId = conversationId,
            content = result,
            role = MessageRole.TOOL.name,
            timestamp = System.currentTimeMillis(),
            toolResultJson = Gson().toJson(ToolResult(toolCallId, result))
        )
        val id = messageDao.insertMessage(toolMessage)
        return Result.success(toolMessage.copy(id = id).toDomainModel())
    }

    private fun MessageEntity.toDomainModel(): Message {
        val images = imagesJson?.let { json ->
            try {
                val type = object : TypeToken<List<MessageImage>>() {}.type
                Gson().fromJson<List<MessageImage>>(json, type)
            } catch (e: Exception) {
                emptyList()
            }
        } ?: emptyList()

        val toolCalls = toolCallsJson?.let { json ->
            try {
                val type = object : TypeToken<List<ToolCall>>() {}.type
                Gson().fromJson<List<ToolCall>>(json, type)
            } catch (e: Exception) {
                emptyList()
            }
        } ?: emptyList()

        val toolResult = toolResultJson?.let { json ->
            try {
                Gson().fromJson(json, ToolResult::class.java)
            } catch (e: Exception) {
                null
            }
        }

        val sources = sourcesJson?.let { json ->
            try {
                val type = object : TypeToken<List<MessageSource>>() {}.type
                Gson().fromJson<List<MessageSource>>(json, type)
            } catch (e: Exception) {
                emptyList()
            }
        } ?: emptyList()

        return Message(
            id = id,
            conversationId = conversationId,
            content = content,
            role = try { MessageRole.valueOf(role) } catch(e: Exception) { MessageRole.USER },
            timestamp = timestamp,
            isError = isError,
            thinking = thinking,
            thinkingSummary = thinkingSummary,
            images = images,
            toolCalls = toolCalls,
            toolResult = toolResult,
            sources = sources
        )
    }

    private fun List<MessageImage>.toJson(): String? {
        return if (isEmpty()) null else Gson().toJson(this)
    }

    private fun AgentSkill.toGeminiFunction(): GeminiFunctionDeclaration {
        val properties = params.associate { param ->
            param.name to GeminiSchemaProperty(
                type = param.type.uppercase(),
                description = param.description,
                enum = param.enumValues
            )
        }
        
        val required = params.filter { it.required }.map { it.name }

        return GeminiFunctionDeclaration(
            name = name,
            description = description,
            parameters = GeminiSchema(
                properties = if (properties.isNotEmpty()) properties else null,
                required = if (required.isNotEmpty()) required else null
            )
        )
    }

    private fun AgentSkill.toAnthropicTool(): AnthropicTool {
        val properties = params.associate { param ->
            param.name to AnthropicSchemaProperty(
                type = param.type.lowercase(),
                description = param.description,
                enum = param.enumValues
            )
        }
        
        val required = params.filter { it.required }.map { it.name }

        return AnthropicTool(
            name = name,
            description = description,
            inputSchema = AnthropicSchema(
                properties = properties,
                required = if (required.isNotEmpty()) required else null
            )
        )
    }

    private fun AgentSkill.toOpenAITool(): OpenAITool {
        val properties = params.associate { param ->
            param.name to OpenAISchemaProperty(
                type = param.type.lowercase(),
                description = param.description,
                enum = param.enumValues
            )
        }
        
        val required = params.filter { it.required }.map { it.name }

        return OpenAITool(
            function = OpenAIFunction(
                name = name,
                description = description,
                parameters = OpenAISchema(
                    properties = properties,
                    required = if (required.isNotEmpty()) required else null
                )
            )
        )
    }
}

private class MutableToolCallBuilder {
    var id: String? = null
    var name: String? = null
    val argumentsBuilder = StringBuilder()
}
