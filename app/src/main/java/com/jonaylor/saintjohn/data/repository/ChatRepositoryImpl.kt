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
import com.jonaylor.saintjohn.data.remote.dto.GeminiContent
import com.jonaylor.saintjohn.data.remote.dto.GeminiGenerationConfig
import com.jonaylor.saintjohn.data.remote.dto.GeminiPart
import com.jonaylor.saintjohn.data.remote.dto.GeminiRequest
import com.jonaylor.saintjohn.data.remote.dto.GeminiStreamChunk
import com.jonaylor.saintjohn.data.remote.dto.OpenAIMessage
import com.jonaylor.saintjohn.data.remote.dto.OpenAIRequest
import com.jonaylor.saintjohn.data.remote.dto.OpenAIStreamChunk
import com.jonaylor.saintjohn.domain.model.LLMProvider
import com.jonaylor.saintjohn.domain.model.Message
import com.jonaylor.saintjohn.domain.model.MessageImage
import com.jonaylor.saintjohn.domain.model.MessageRole
import com.google.gson.reflect.TypeToken
import com.jonaylor.saintjohn.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao,
    private val preferencesManager: PreferencesManager,
    private val openAIApi: OpenAIApi,
    private val geminiApi: GeminiApi,
    private val anthropicApi: AnthropicApi
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
        content: String,
        provider: LLMProvider
    ): Result<Message> {
        return try {
            // Save user message
            val userMessage = MessageEntity(
                conversationId = conversationId,
                content = content,
                role = MessageRole.USER.name,
                timestamp = System.currentTimeMillis()
            )
            messageDao.insertMessage(userMessage)

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
            // Save error message for user to see
            val errorMessage = MessageEntity(
                conversationId = conversationId,
                content = "Error: ${e.message ?: "Unknown error occurred"}",
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
        content: String,
        provider: LLMProvider,
        onChunk: (String) -> Unit
    ): Result<Message> {
        return try {
            // Save user message
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
                            OpenAIMessage(
                                role = msg.role.lowercase(),
                                content = msg.content
                            )
                        })
                    }

                    // Get selected model for this provider
                    val selectedModel = getSelectedModel(provider)

                    // Make streaming API call
                    val request = OpenAIRequest(
                        model = selectedModel,
                        messages = openAIMessages,
                        stream = true
                    )

                    val responseBody = openAIApi.createChatCompletionStream(
                        authorization = "Bearer $apiKey",
                        request = request
                    )

                    val fullContent = StringBuilder()
                    val fullThinking = StringBuilder()
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
                                } catch (e: Exception) {
                                    // Skip malformed chunks
                                }
                            }
                    }

                    // Store final thinking content
                    if (fullThinking.isNotEmpty()) {
                        messageDao.updateMessage(
                            assistantMessage.copy(
                                id = messageId,
                                content = fullContent.toString(),
                                thinking = fullThinking.toString()
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
                        AnthropicMessage(
                            role = msg.role.lowercase(),
                            content = msg.content
                        )
                    }

                    // Get selected model for this provider
                    val selectedModel = getSelectedModel(provider)

                    // Make streaming API call with system prompt
                    val request = AnthropicRequest(
                        model = selectedModel,
                        messages = anthropicMessages,
                        stream = true,
                        system = systemPrompt.takeIf { it.isNotBlank() }
                    )

                    val responseBody = anthropicApi.createMessageStream(
                        apiKey = apiKey,
                        request = request
                    )

                    val fullContent = StringBuilder()
                    val fullThinking = StringBuilder()
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
                                                // Track the type of the current block (thinking vs text)
                                                currentBlockType = chunk.contentBlock?.type
                                            }
                                            "content_block_delta" -> {
                                                val deltaType = chunk.delta?.type
                                                val text = chunk.delta?.text
                                                
                                                // Handle thinking delta (extended thinking)
                                                if (deltaType == "thinking_delta" || currentBlockType == "thinking") {
                                                    text?.let { thinking ->
                                                        fullThinking.append(thinking)
                                                        messageDao.updateMessage(
                                                            assistantMessage.copy(
                                                                id = messageId,
                                                                content = fullContent.toString(),
                                                                thinking = fullThinking.toString()
                                                            )
                                                        )
                                                    }
                                                } else {
                                                    // Handle regular text delta
                                                    text?.let { content ->
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
                                            }
                                            "content_block_stop" -> {
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
                    if (fullThinking.isNotEmpty()) {
                        messageDao.updateMessage(
                            assistantMessage.copy(
                                id = messageId,
                                content = fullContent.toString(),
                                thinking = fullThinking.toString()
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
                        GeminiContent(
                            parts = listOf(GeminiPart(text = msg.content)),
                            role = if (msg.role == "USER") "user" else "model"
                        )
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

                    val request = GeminiRequest(
                        contents = geminiContents,
                        systemInstruction = systemInstruction,
                        generationConfig = generationConfig
                    )

                    val responseBody = geminiApi.streamGenerateContent(
                        model = selectedModel,
                        apiKey = apiKey,
                        request = request
                    )

                    val fullContent = StringBuilder()
                    val collectedImages = mutableListOf<MessageImage>()
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
                                        }

                                        // Update the message in database in real-time
                                        messageDao.updateMessage(
                                            assistantMessage.copy(
                                                id = messageId,
                                                content = fullContent.toString(),
                                                imagesJson = collectedImages.toJson()
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

            // Message was already inserted and updated during streaming
            // Just return the final version
            Result.success(assistantMessage.copy(id = messageId, content = assistantContent).toDomainModel())
        } catch (e: Exception) {
            // Save error message for user to see
            val errorMessage = MessageEntity(
                conversationId = conversationId,
                content = "Error: ${e.message ?: "Unknown error occurred"}",
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
                "claude-sonnet-4-5-20250929",
                "claude-haiku-4-5-20251001",
                "claude-opus-4-1-20250805"
            )
            LLMProvider.GOOGLE -> listOf(
                "gemini-2.5-pro",
                "gemini-2.5-flash",
                "gemini-2.0-flash"
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

    private fun MessageEntity.toDomainModel(): Message {
        val images = imagesJson?.let { json ->
            try {
                val type = object : TypeToken<List<MessageImage>>() {}.type
                Gson().fromJson<List<MessageImage>>(json, type)
            } catch (e: Exception) {
                emptyList()
            }
        } ?: emptyList()

        return Message(
            id = id,
            conversationId = conversationId,
            content = content,
            role = MessageRole.valueOf(role),
            timestamp = timestamp,
            isError = isError,
            thinking = thinking,
            thinkingSummary = thinkingSummary,
            images = images
        )
    }

    private fun List<MessageImage>.toJson(): String? {
        return if (isEmpty()) null else Gson().toJson(this)
    }
}
