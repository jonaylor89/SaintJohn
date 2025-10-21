package com.jonaylor.saintjohn.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    var openaiKey by remember { mutableStateOf("") }
    var anthropicKey by remember { mutableStateOf("") }
    var googleKey by remember { mutableStateOf("") }

    // Load API keys when settings dialog opens
    LaunchedEffect(showSettings) {
        if (showSettings) {
            openaiKey = viewModel.getOpenAIKey()
            anthropicKey = viewModel.getAnthropicKey()
            googleKey = viewModel.getGoogleKey()
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
                    CombinedModelSelector(
                        selectedProvider = uiState.selectedProvider,
                        selectedModel = uiState.selectedModel,
                        availableModels = uiState.availableModels,
                        isLoadingModels = uiState.isLoadingModels,
                        onModelSelected = { viewModel.selectModel(it) },
                        onLoadModels = { viewModel.loadAvailableModels() }
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = { viewModel.newConversation() }) {
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
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.messages, key = { it.id }) { message ->
                        ChatBubble(message = message)
                    }
                }
            }

            // Input Bar
            ChatInput(
                onSendMessage = { content ->
                    viewModel.sendMessage(content)
                },
                isLoading = uiState.isLoading
            )
        }

        // Settings Bottom Sheet
        if (showSettings) {
            SettingsBottomSheet(
                openaiKey = openaiKey,
                anthropicKey = anthropicKey,
                googleKey = googleKey,
                onOpenAIKeyChange = { openaiKey = it },
                onAnthropicKeyChange = { anthropicKey = it },
                onGoogleKeyChange = { googleKey = it },
                onDismiss = { showSettings = false },
                onSave = {
                    coroutineScope.launch {
                        viewModel.saveApiKeys(openaiKey, anthropicKey, googleKey)
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
    }
}
