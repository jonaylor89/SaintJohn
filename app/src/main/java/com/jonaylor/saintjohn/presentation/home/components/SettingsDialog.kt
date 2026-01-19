package com.jonaylor.saintjohn.presentation.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    openaiKey: String,
    anthropicKey: String,
    googleKey: String,
    tavilyKey: String,
    systemPrompt: String,
    showToolResults: Boolean,
    onOpenAIKeyChange: (String) -> Unit,
    onAnthropicKeyChange: (String) -> Unit,
    onGoogleKeyChange: (String) -> Unit,
    onTavilyKeyChange: (String) -> Unit,
    onSystemPromptChange: (String) -> Unit,
    onShowToolResultsChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var localOpenAIKey by remember { mutableStateOf(openaiKey) }
    var localAnthropicKey by remember { mutableStateOf(anthropicKey) }
    var localGoogleKey by remember { mutableStateOf(googleKey) }
    var localTavilyKey by remember { mutableStateOf(tavilyKey) }
    var localSystemPrompt by remember { mutableStateOf(systemPrompt) }
    var localShowToolResults by remember { mutableStateOf(showToolResults) }

    // Update local state when props change (e.g., when dialog reopens with new values)
    LaunchedEffect(openaiKey, anthropicKey, googleKey, tavilyKey, systemPrompt, showToolResults) {
        localOpenAIKey = openaiKey
        localAnthropicKey = anthropicKey
        localGoogleKey = googleKey
        localTavilyKey = tavilyKey
        localSystemPrompt = systemPrompt
        localShowToolResults = showToolResults
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
                Text(
                    text = "API Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Configure your API keys to use the chat feature. Keys are stored locally on your device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // OpenAI API Key
                ApiKeyField(
                    label = "OpenAI API Key",
                    value = localOpenAIKey,
                    onValueChange = { localOpenAIKey = it },
                    placeholder = "sk-..."
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Anthropic API Key
                ApiKeyField(
                    label = "Anthropic API Key",
                    value = localAnthropicKey,
                    onValueChange = { localAnthropicKey = it },
                    placeholder = "sk-ant-..."
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Google API Key
                ApiKeyField(
                    label = "Google API Key",
                    value = localGoogleKey,
                    onValueChange = { localGoogleKey = it },
                    placeholder = "AIza..."
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tavily API Key
                ApiKeyField(
                    label = "Tavily API Key (Web Search)",
                    value = localTavilyKey,
                    onValueChange = { localTavilyKey = it },
                    placeholder = "tvly-..."
                )

                Spacer(modifier = Modifier.height(24.dp))

                HorizontalDivider()

                Spacer(modifier = Modifier.height(24.dp))

                // System Prompt
                Text(
                    text = "System Prompt",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Customize how the AI responds. This applies to all conversations.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = localSystemPrompt,
                    onValueChange = { localSystemPrompt = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    placeholder = { Text("e.g., You are a helpful assistant. Be concise and direct.") },
                    minLines = 3,
                    maxLines = 6,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                HorizontalDivider()

                Spacer(modifier = Modifier.height(24.dp))

                // Developer Options
                Text(
                    text = "Developer Options",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Show Tool Results",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Display raw tool call results in chat",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = localShowToolResults,
                        onCheckedChange = { localShowToolResults = it }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            onOpenAIKeyChange(localOpenAIKey)
                            onAnthropicKeyChange(localAnthropicKey)
                            onGoogleKeyChange(localGoogleKey)
                            onTavilyKeyChange(localTavilyKey)
                            onSystemPromptChange(localSystemPrompt)
                            onShowToolResultsChange(localShowToolResults)
                            onSave()
                        }
                    ) {
                        Text("Save")
                    }
                }
        }
    }
}

@Composable
private fun ApiKeyField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    var isVisible by remember { mutableStateOf(false) }

    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder) },
            visualTransformation = if (isVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                TextButton(onClick = { isVisible = !isVisible }) {
                    Text(
                        text = if (isVisible) "Hide" else "Show",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}
