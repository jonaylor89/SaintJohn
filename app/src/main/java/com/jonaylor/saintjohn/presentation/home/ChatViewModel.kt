package com.jonaylor.saintjohn.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonaylor.saintjohn.data.local.PreferencesManager
import com.jonaylor.saintjohn.domain.model.LLMProvider
import com.jonaylor.saintjohn.domain.model.Message
import com.jonaylor.saintjohn.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.jonaylor.saintjohn.domain.agent.SkillRegistry
import com.jonaylor.saintjohn.domain.model.MessageRole

data class ModelInfo(
    val name: String,
    val provider: LLMProvider,
    val isLocked: Boolean
)

data class ConversationWithCount(
    val conversation: com.jonaylor.saintjohn.data.local.entity.ConversationEntity,
    val messageCount: Int
)

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val selectedProvider: LLMProvider = LLMProvider.ANTHROPIC,
    val selectedModel: String = "",
    val availableModels: List<ModelInfo> = emptyList(),
    val isLoadingModels: Boolean = false,
    val conversations: List<ConversationWithCount> = emptyList(),
    val currentConversationId: Long? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val preferencesManager: PreferencesManager,
    private val skillRegistry: SkillRegistry
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentConversationId: Long? = null
    private var sendMessageJob: Job? = null
    private var messagesCollectionJob: Job? = null

    init {
        loadSelectedProvider()
        loadMostRecentConversation()
        loadSelectedModel()
        loadConversations()
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

    private fun loadMostRecentConversation() {
        viewModelScope.launch {
            // Try to get the most recent conversation with messages
            currentConversationId = chatRepository.getMostRecentConversationId()
            currentConversationId?.let { id ->
                // Load the conversation to get its provider and model
                val conversations = chatRepository.getAllConversations().first()
                val conversation = conversations.find { it.id == id }

                conversation?.let {
                    val provider = try {
                        LLMProvider.valueOf(it.provider)
                    } catch (e: Exception) {
                        LLMProvider.ANTHROPIC
                    }

                    _uiState.value = _uiState.value.copy(
                        currentConversationId = id,
                        selectedProvider = provider,
                        selectedModel = it.model
                    )
                }

                // Cancel any existing message collection and start a new one
                messagesCollectionJob?.cancel()
                messagesCollectionJob = viewModelScope.launch {
                    chatRepository.getMessages(id).collect { messages ->
                        _uiState.value = _uiState.value.copy(messages = messages)
                    }
                }
            }
            // If null, we'll start with an empty state (no conversation yet)
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return

        sendMessageJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Create conversation only when first message is sent
            if (currentConversationId == null) {
                currentConversationId = chatRepository.createNewConversation(
                    _uiState.value.selectedProvider,
                    _uiState.value.selectedModel
                )
                _uiState.value = _uiState.value.copy(currentConversationId = currentConversationId)

                // Cancel any existing message collection and start observing this new conversation
                currentConversationId?.let { id ->
                    messagesCollectionJob?.cancel()
                    messagesCollectionJob = viewModelScope.launch {
                        chatRepository.getMessages(id).collect { messages ->
                            _uiState.value = _uiState.value.copy(messages = messages)
                        }
                    }
                }
            }

            val conversationId = currentConversationId!!

            chatRepository.sendMessageStreaming(
                conversationId = conversationId,
                content = content,
                provider = _uiState.value.selectedProvider,
                onChunk = { chunk ->
                    // The UI will automatically update from the database flow
                }
            )

            _uiState.value = _uiState.value.copy(isLoading = false)
            
            // Check for tool calls
            checkForToolCalls(conversationId)
        }
    }
    
    private suspend fun checkForToolCalls(conversationId: Long) {
        val messages = chatRepository.getMessages(conversationId).first()
        val lastMessage = messages.lastOrNull()
        
        if (lastMessage != null && 
            lastMessage.role == MessageRole.ASSISTANT && 
            !lastMessage.isError && 
            lastMessage.toolCalls.isNotEmpty()
        ) {
            executeTools(conversationId, lastMessage)
        }
    }
    
    private suspend fun executeTools(conversationId: Long, message: Message) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        message.toolCalls.forEach { toolCall ->
            val skill = skillRegistry.getSkill(toolCall.name)
            val result = if (skill != null) {
                try {
                    skill.execute(toolCall.arguments)
                } catch (e: Exception) {
                    "Error executing skill: ${e.message}"
                }
            } else {
                "Error: Skill '${toolCall.name}' not found."
            }
            
            chatRepository.sendToolResult(conversationId, toolCall.id, result)
        }
        
        // Continue conversation
        chatRepository.sendMessageStreaming(
            conversationId = conversationId,
            content = null, // Continue without user input
            provider = _uiState.value.selectedProvider,
            onChunk = { } // No-op, UI updates from DB flow
        )
        
        _uiState.value = _uiState.value.copy(isLoading = false)
        
        // Recursive check for multi-turn agentic loop
        checkForToolCalls(conversationId)
    }

    fun cancelMessage() {
        sendMessageJob?.cancel()
        sendMessageJob = null
        _uiState.value = _uiState.value.copy(isLoading = false)

        // Clean up any empty "Thinking..." placeholder messages
        currentConversationId?.let { id ->
            viewModelScope.launch {
                chatRepository.deleteEmptyAssistantMessages(id)
            }
        }
    }

    fun selectProvider(provider: LLMProvider) {
        viewModelScope.launch {
            preferencesManager.setSelectedLLMProvider(provider.name)
            _uiState.value = _uiState.value.copy(selectedProvider = provider)
            // Load the selected model for the new provider
            val model = chatRepository.getSelectedModel(provider)
            _uiState.value = _uiState.value.copy(selectedModel = model)
        }
    }

    fun newConversation() {
        viewModelScope.launch {
            // Just clear the current conversation - don't create a new one until first message
            currentConversationId = null
            _uiState.value = _uiState.value.copy(
                currentConversationId = null,
                messages = emptyList()
            )
        }
    }

    suspend fun getOpenAIKey(): String {
        return preferencesManager.openaiApiKey.firstOrNull() ?: ""
    }

    suspend fun getAnthropicKey(): String {
        return preferencesManager.anthropicApiKey.firstOrNull() ?: ""
    }

    suspend fun getGoogleKey(): String {
        return preferencesManager.googleApiKey.firstOrNull() ?: ""
    }

    suspend fun getSystemPrompt(): String {
        return preferencesManager.systemPrompt.firstOrNull() ?: ""
    }

    suspend fun saveSettings(openaiKey: String, anthropicKey: String, googleKey: String, systemPrompt: String) {
        preferencesManager.setOpenAIApiKey(openaiKey)
        preferencesManager.setAnthropicApiKey(anthropicKey)
        preferencesManager.setGoogleApiKey(googleKey)
        preferencesManager.setSystemPrompt(systemPrompt)
    }

    @Deprecated("Use saveSettings instead", ReplaceWith("saveSettings(openaiKey, anthropicKey, googleKey, \"\")"))
    suspend fun saveApiKeys(openaiKey: String, anthropicKey: String, googleKey: String) {
        saveSettings(openaiKey, anthropicKey, googleKey, "")
    }

    private fun loadSelectedModel() {
        viewModelScope.launch {
            val model = chatRepository.getSelectedModel(_uiState.value.selectedProvider)
            _uiState.value = _uiState.value.copy(selectedModel = model)
        }
    }

    fun loadAvailableModels() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingModels = true)

            // Get API keys to determine which models are locked
            val openaiKey = preferencesManager.openaiApiKey.first()
            val anthropicKey = preferencesManager.anthropicApiKey.first()
            val googleKey = preferencesManager.googleApiKey.first()

            // Load models from all providers
            val allModels = mutableListOf<ModelInfo>()

            LLMProvider.entries.forEach { provider ->
                val result = chatRepository.getAvailableModels(provider)
                result.onSuccess { models ->
                    val isLocked = when (provider) {
                        LLMProvider.OPENAI -> openaiKey.isBlank()
                        LLMProvider.ANTHROPIC -> anthropicKey.isBlank()
                        LLMProvider.GOOGLE -> googleKey.isBlank()
                    }

                    models.forEach { modelName ->
                        allModels.add(ModelInfo(
                            name = modelName,
                            provider = provider,
                            isLocked = isLocked
                        ))
                    }
                }
            }

            _uiState.value = _uiState.value.copy(
                availableModels = allModels,
                isLoadingModels = false
            )
        }
    }

    fun selectModel(modelInfo: ModelInfo) {
        viewModelScope.launch {
            // Update both provider and model
            preferencesManager.setSelectedLLMProvider(modelInfo.provider.name)
            chatRepository.setSelectedModel(modelInfo.provider, modelInfo.name)
            _uiState.value = _uiState.value.copy(
                selectedProvider = modelInfo.provider,
                selectedModel = modelInfo.name
            )
        }
    }

    private fun loadConversations() {
        viewModelScope.launch {
            chatRepository.getAllConversations().collect { conversations ->
                val conversationsWithCounts = conversations.map { conversation ->
                    val count = chatRepository.getMessageCount(conversation.id)
                    ConversationWithCount(conversation, count)
                }
                _uiState.value = _uiState.value.copy(conversations = conversationsWithCounts)
            }
        }
    }

    fun switchConversation(conversationId: Long) {
        viewModelScope.launch {
            chatRepository.switchToConversation(conversationId)
            currentConversationId = conversationId

            // Load the conversation to get its provider and model
            val conversations = chatRepository.getAllConversations().first()
            val conversation = conversations.find { it.id == conversationId }

            conversation?.let {
                val provider = try {
                    LLMProvider.valueOf(it.provider)
                } catch (e: Exception) {
                    LLMProvider.ANTHROPIC
                }

                _uiState.value = _uiState.value.copy(
                    currentConversationId = conversationId,
                    selectedProvider = provider,
                    selectedModel = it.model
                )
            }

            // Cancel existing message collection and start a new one for this conversation
            messagesCollectionJob?.cancel()
            messagesCollectionJob = viewModelScope.launch {
                chatRepository.getMessages(conversationId).collect { messages ->
                    _uiState.value = _uiState.value.copy(messages = messages)
                }
            }
        }
    }

    fun deleteConversation(conversationWithCount: ConversationWithCount) {
        viewModelScope.launch {
            chatRepository.deleteConversation(conversationWithCount.conversation)
            // If we deleted the current conversation, create a new one
            if (conversationWithCount.conversation.id == currentConversationId) {
                newConversation()
            }
        }
    }
}