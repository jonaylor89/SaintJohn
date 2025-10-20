package com.jonaylor.saintjohn.data.repository

import com.jonaylor.saintjohn.data.local.PreferencesManager
import com.jonaylor.saintjohn.data.local.dao.ConversationDao
import com.jonaylor.saintjohn.data.local.dao.MessageDao
import com.jonaylor.saintjohn.data.local.entity.ConversationEntity
import com.jonaylor.saintjohn.data.local.entity.MessageEntity
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
    private val preferencesManager: PreferencesManager
) : ChatRepository {

    private var currentConversationId: Long? = null

    override fun getMessages(conversationId: Long): Flow<List<Message>> {
        return messageDao.getMessagesByConversation(conversationId).map { entities ->
            entities.map { it.toDomainModel() }
        }
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

            // TODO: Make actual API call here
            // For now, return a placeholder response
            val assistantMessage = MessageEntity(
                conversationId = conversationId,
                content = "API integration coming soon! Set your API key in settings and this will connect to ${provider.displayName}.",
                role = MessageRole.ASSISTANT.name,
                timestamp = System.currentTimeMillis()
            )
            val id = messageDao.insertMessage(assistantMessage)

            Result.success(assistantMessage.copy(id = id).toDomainModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createNewConversation(provider: LLMProvider): Long {
        val conversation = ConversationEntity(
            title = "New Chat",
            provider = provider.name,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val id = conversationDao.insertConversation(conversation)
        currentConversationId = id
        return id
    }

    override suspend fun getCurrentConversationId(): Long {
        return currentConversationId ?: run {
            val provider = LLMProvider.valueOf(
                preferencesManager.selectedLLMProvider.first()
            )
            createNewConversation(provider)
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
