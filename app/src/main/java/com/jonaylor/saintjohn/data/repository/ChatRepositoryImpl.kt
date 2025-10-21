package com.jonaylor.saintjohn.data.repository

import com.jonaylor.saintjohn.data.local.PreferencesManager
import com.jonaylor.saintjohn.data.local.dao.ConversationDao
import com.jonaylor.saintjohn.data.local.dao.MessageDao
import com.jonaylor.saintjohn.data.local.entity.ConversationEntity
import com.jonaylor.saintjohn.data.local.entity.MessageEntity
import com.google.gson.Gson
import com.jonaylor.saintjohn.data.remote.OpenAIApi
import com.jonaylor.saintjohn.data.remote.dto.OpenAIMessage
import com.jonaylor.saintjohn.data.remote.dto.OpenAIRequest
import com.jonaylor.saintjohn.data.remote.dto.OpenAIStreamChunk
import com.jonaylor.saintjohn.domain.model.LLMProvider
import com.jonaylor.saintjohn.domain.model.Message
import com.jonaylor.saintjohn.domain.model.MessageRole
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
    private val openAIApi: OpenAIApi
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
                    // TODO: Implement Anthropic API
                    "Anthropic API integration coming soon!"
                }

                LLMProvider.GOOGLE -> {
                    // TODO: Implement Google API
                    "Google API integration coming soon!"
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

            // Make streaming API call
            val assistantContent = when (provider) {
                LLMProvider.OPENAI -> {
                    // Get conversation history for context
                    val history = messageDao.getMessagesByConversation(conversationId).first()
                        .filter { it.id != messageId } // Exclude the placeholder

                    // Convert to OpenAI format
                    val openAIMessages = history.map { msg ->
                        OpenAIMessage(
                            role = msg.role.lowercase(),
                            content = msg.content
                        )
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
                                    chunk.choices.firstOrNull()?.delta?.content?.let { content ->
                                        fullContent.append(content)

                                        // Update the message in database in real-time
                                        messageDao.updateMessage(
                                            assistantMessage.copy(
                                                id = messageId,
                                                content = fullContent.toString()
                                            )
                                        )

                                        onChunk(content)
                                    }
                                } catch (e: Exception) {
                                    // Skip malformed chunks
                                }
                            }
                    }

                    fullContent.toString()
                }

                LLMProvider.ANTHROPIC -> {
                    // TODO: Implement Anthropic streaming
                    "Anthropic streaming not implemented yet"
                }

                LLMProvider.GOOGLE -> {
                    // TODO: Implement Google streaming
                    "Google streaming not implemented yet"
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
            when (provider) {
                LLMProvider.OPENAI -> {
                    // Curated list of latest/popular OpenAI models
                    val popularModels = listOf(
                        "o1",              // Latest reasoning model
                        "o1-mini",         // Fast reasoning model
                        "gpt-4o",          // Latest flagship
                        "gpt-4o-mini",     // Fast and cheap
                        "gpt-4-turbo",     // Previous gen turbo
                        "gpt-3.5-turbo"    // Legacy fast model
                    )
                    Result.success(popularModels)
                }

                LLMProvider.ANTHROPIC -> {
                    // TODO: Implement Anthropic models fetch
                    // For now, return hardcoded list
                    Result.success(listOf(
                        "claude-3-5-sonnet-20241022",
                        "claude-3-5-haiku-20241022",
                        "claude-3-opus-20240229"
                    ))
                }

                LLMProvider.GOOGLE -> {
                    // TODO: Implement Google models fetch
                    // For now, return hardcoded list
                    Result.success(listOf(
                        "gemini-pro",
                        "gemini-pro-vision"
                    ))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
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
        return Message(
            id = id,
            conversationId = conversationId,
            content = content,
            role = MessageRole.valueOf(role),
            timestamp = timestamp,
            isError = isError
        )
    }
}
