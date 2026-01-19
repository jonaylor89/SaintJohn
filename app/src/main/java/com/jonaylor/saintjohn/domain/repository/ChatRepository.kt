package com.jonaylor.saintjohn.domain.repository

import com.jonaylor.saintjohn.data.local.entity.ConversationEntity
import com.jonaylor.saintjohn.domain.model.Message
import com.jonaylor.saintjohn.domain.model.MessageSource
import com.jonaylor.saintjohn.domain.model.LLMProvider
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getMessages(conversationId: Long): Flow<List<Message>>
    fun getAllConversations(): Flow<List<ConversationEntity>>
    suspend fun getMessageCount(conversationId: Long): Int
    suspend fun sendMessage(conversationId: Long, content: String?, provider: LLMProvider): Result<Message>
    suspend fun sendMessageStreaming(conversationId: Long, content: String?, provider: LLMProvider, onChunk: (String) -> Unit, sources: List<MessageSource> = emptyList()): Result<Message>
    suspend fun createNewConversation(provider: LLMProvider, model: String): Long
    suspend fun getMostRecentConversationId(): Long?
    suspend fun switchToConversation(conversationId: Long)
    suspend fun deleteConversation(conversation: ConversationEntity)
    suspend fun deleteEmptyAssistantMessages(conversationId: Long)
    suspend fun getAvailableModels(provider: LLMProvider): Result<List<String>>
    suspend fun getSelectedModel(provider: LLMProvider): String
    suspend fun setSelectedModel(provider: LLMProvider, model: String)
    suspend fun sendToolResult(conversationId: Long, toolCallId: String, result: String): Result<Message>
}
