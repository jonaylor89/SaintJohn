package com.jonaylor.saintjohn.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jonaylor.saintjohn.presentation.home.components.ChatBubble
import com.jonaylor.saintjohn.presentation.home.components.ChatInput
import com.jonaylor.saintjohn.presentation.home.components.CombinedModelSelector
import com.jonaylor.saintjohn.presentation.home.components.ConversationHistoryBottomSheet
import com.jonaylor.saintjohn.presentation.home.components.SettingsBottomSheet
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var showSettings by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var showModelSelector by remember { mutableStateOf(false) }
    var openaiKey by remember { mutableStateOf("") }
    var anthropicKey by remember { mutableStateOf("") }
    var googleKey by remember { mutableStateOf("") }
    var tavilyKey by remember { mutableStateOf("") }
    var systemPrompt by remember { mutableStateOf("") }
    var showToolResults by remember { mutableStateOf(false) }

    // Load API keys and system prompt when settings dialog opens
    LaunchedEffect(showSettings) {
        if (showSettings) {
            openaiKey = viewModel.getOpenAIKey()
            anthropicKey = viewModel.getAnthropicKey()
            googleKey = viewModel.getGoogleKey()
            tavilyKey = viewModel.getTavilyKey()
            systemPrompt = viewModel.getSystemPrompt()
            showToolResults = viewModel.getShowToolResults()
        }
    }


    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Always display current model as read-only
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (uiState.selectedModel.isNotEmpty()) {
                                "${getShortProviderName(uiState.selectedProvider.displayName)} - ${getShortModelName(uiState.selectedModel)}"
                            } else {
                                getShortProviderName(uiState.selectedProvider.displayName)
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = {
                        showModelSelector = true
                        viewModel.loadAvailableModels()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Conversation",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    IconButton(onClick = { showHistory = true }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search History",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    IconButton(onClick = { showSettings = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            Divider()

            // Messages List
            if (uiState.messages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "let's chat",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Filter out TOOL messages unless showToolResults is enabled
                val displayMessages = if (uiState.showToolResults) {
                    uiState.messages
                } else {
                    uiState.messages.filter { it.role != com.jonaylor.saintjohn.domain.model.MessageRole.TOOL }
                }
                
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayMessages, key = { it.id }) { message ->
                        ChatBubble(message = message)
                    }
                }
            }

            // Input Bar
            ChatInput(
                onSendMessage = { content ->
                    viewModel.sendMessage(content)
                },
                isLoading = uiState.isLoading,
                onCancelMessage = {
                    viewModel.cancelMessage()
                }
            )
        }

        // Settings Bottom Sheet
        if (showSettings) {
            SettingsBottomSheet(
                openaiKey = openaiKey,
                anthropicKey = anthropicKey,
                googleKey = googleKey,
                tavilyKey = tavilyKey,
                systemPrompt = systemPrompt,
                showToolResults = showToolResults,
                onOpenAIKeyChange = { openaiKey = it },
                onAnthropicKeyChange = { anthropicKey = it },
                onGoogleKeyChange = { googleKey = it },
                onTavilyKeyChange = { tavilyKey = it },
                onSystemPromptChange = { systemPrompt = it },
                onShowToolResultsChange = { showToolResults = it },
                onDismiss = { showSettings = false },
                onSave = {
                    coroutineScope.launch {
                        viewModel.saveSettings(openaiKey, anthropicKey, googleKey, tavilyKey, systemPrompt, showToolResults)
                        showSettings = false
                    }
                }
            )
        }

        // Conversation History Bottom Sheet
        if (showHistory) {
            ConversationHistoryBottomSheet(
                conversations = uiState.conversations,
                currentConversationId = uiState.currentConversationId,
                onConversationSelected = { conversationId ->
                    viewModel.switchConversation(conversationId)
                },
                onConversationDeleted = { conversation ->
                    viewModel.deleteConversation(conversation)
                },
                onDismiss = { showHistory = false }
            )
        }

        // Model Selector Bottom Sheet (for new conversations)
        if (showModelSelector) {
            ModelSelectorBottomSheet(
                selectedModel = uiState.selectedModel,
                availableModels = uiState.availableModels,
                isLoadingModels = uiState.isLoadingModels,
                onModelSelected = { modelInfo ->
                    viewModel.selectModel(modelInfo)
                    viewModel.newConversation()
                    showModelSelector = false
                },
                onLoadModels = { viewModel.loadAvailableModels() },
                onDismiss = { showModelSelector = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModelSelectorBottomSheet(
    selectedModel: String,
    availableModels: List<ModelInfo>,
    isLoadingModels: Boolean,
    onModelSelected: (ModelInfo) -> Unit,
    onLoadModels: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select Model",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                TextButton(onClick = onLoadModels) {
                    Text("Refresh")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoadingModels) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (availableModels.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No models available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = onLoadModels) {
                            Text("Try Again")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(availableModels) { modelInfo ->
                        ModelItem(
                            modelInfo = modelInfo,
                            isSelected = modelInfo.name == selectedModel,
                            onClick = {
                                if (!modelInfo.isLocked) {
                                    onModelSelected(modelInfo)
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ModelItem(
    modelInfo: ModelInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !modelInfo.isLocked, onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else if (modelInfo.isLocked) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = modelInfo.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else if (modelInfo.isLocked) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Text(
                    text = modelInfo.provider.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else if (modelInfo.isLocked) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    }
                )
            }

            if (modelInfo.isLocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked - API key required",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            } else if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

// Helper functions to shorten model and provider names
private fun getShortProviderName(providerName: String): String {
    return when (providerName.lowercase()) {
        "openai" -> "OpenAI"
        "anthropic" -> "Anthropic"
        "google" -> "Google"
        else -> providerName
    }
}

private fun getShortModelName(modelName: String): String {
    // Map long Anthropic model names to shorter versions
    return when {
        modelName.contains("claude-sonnet-4-5") -> "Sonnet 4.5"
        modelName.contains("claude-haiku-4-5") -> "Haiku 4.5"
        modelName.contains("claude-opus-4-1") -> "Opus 4.1"
        modelName.contains("claude-sonnet-4") -> "Sonnet 4"
        modelName.contains("claude-haiku-4") -> "Haiku 4"
        modelName.contains("claude-opus-4") -> "Opus 4"
        modelName.contains("claude") -> modelName.substringAfter("claude-")

        // Shorten Gemini models
        modelName.contains("gemini-2.5-flash") -> "Gemini 2.5 Flash"
        modelName.contains("gemini-2.5-pro") -> "Gemini 2.5 Pro"
        modelName.contains("gemini-2.0") -> "Gemini 2.0"
        modelName.contains("gemini-1.5") -> "Gemini 1.5"

        // OpenAI models are already short, but remove any timestamps
        modelName.startsWith("gpt") -> modelName
        modelName.startsWith("o") -> modelName

        else -> modelName
    }
}
