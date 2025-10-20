package com.jonaylor.saintjohn.domain.repository

import com.jonaylor.saintjohn.domain.model.Message
import com.jonaylor.saintjohn.domain.model.LLMProvider
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getMessages(conversationId: Long): Flow<List<Message>>
    suspend fun sendMessage(conversationId: Long, content: String, provider: LLMProvider): Result<Message>
    suspend fun createNewConversation(provider: LLMProvider): Long
    suspend fun getCurrentConversationId(): Long
}
