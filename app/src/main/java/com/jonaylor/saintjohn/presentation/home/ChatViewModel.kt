package com.jonaylor.saintjohn.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonaylor.saintjohn.data.local.PreferencesManager
import com.jonaylor.saintjohn.domain.model.LLMProvider
import com.jonaylor.saintjohn.domain.model.Message
import com.jonaylor.saintjohn.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val selectedProvider: LLMProvider = LLMProvider.ANTHROPIC
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentConversationId: Long? = null

    init {
        loadSelectedProvider()
        loadOrCreateConversation()
    }

    private fun loadSelectedProvider() {
        viewModelScope.launch {
            preferencesManager.selectedLLMProvider.collect { providerName ->
                _uiState.value = _uiState.value.copy(
                    selectedProvider = try {
                        LLMProvider.valueOf(providerName)
                    } catch (e: Exception) {
                        LLMProvider.ANTHROPIC
                    }
                )
            }
        }
    }

    private fun loadOrCreateConversation() {
        viewModelScope.launch {
            currentConversationId = chatRepository.getCurrentConversationId()
            currentConversationId?.let { id ->
                chatRepository.getMessages(id).collect { messages ->
                    _uiState.value = _uiState.value.copy(messages = messages)
                }
            }
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val conversationId = currentConversationId ?: chatRepository.getCurrentConversationId()
            currentConversationId = conversationId

            chatRepository.sendMessage(
                conversationId = conversationId,
                content = content,
                provider = _uiState.value.selectedProvider
            )

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun selectProvider(provider: LLMProvider) {
        viewModelScope.launch {
            preferencesManager.setSelectedLLMProvider(provider.name)
            _uiState.value = _uiState.value.copy(selectedProvider = provider)
        }
    }

    fun newConversation() {
        viewModelScope.launch {
            currentConversationId = chatRepository.createNewConversation(_uiState.value.selectedProvider)
            loadOrCreateConversation()
        }
    }
}
