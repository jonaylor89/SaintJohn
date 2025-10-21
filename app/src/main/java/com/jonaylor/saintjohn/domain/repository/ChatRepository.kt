package com.jonaylor.saintjohn.domain.repository

import com.jonaylor.saintjohn.data.local.entity.ConversationEntity
import com.jonaylor.saintjohn.domain.model.Message
import com.jonaylor.saintjohn.domain.model.LLMProvider
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getMessages(conversationId: Long): Flow<List<Message>>
    fun getAllConversations(): Flow<List<ConversationEntity>>
    suspend fun sendMessage(conversationId: Long, content: String, provider: LLMProvider): Result<Message>
    suspend fun sendMessageStreaming(conversationId: Long, content: String, provider: LLMProvider, onChunk: (String) -> Unit): Result<Message>
    suspend fun createNewConversation(provider: LLMProvider): Long
    suspend fun getMostRecentConversationId(): Long?
    suspend fun switchToConversation(conversationId: Long)
    suspend fun deleteConversation(conversation: ConversationEntity)
    suspend fun getAvailableModels(provider: LLMProvider): Result<List<String>>
    suspend fun getSelectedModel(provider: LLMProvider): String
    suspend fun setSelectedModel(provider: LLMProvider, model: String)
}
