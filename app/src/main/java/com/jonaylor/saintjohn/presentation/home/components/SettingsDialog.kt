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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    openaiKey: String,
    anthropicKey: String,
    googleKey: String,
    onOpenAIKeyChange: (String) -> Unit,
    onAnthropicKeyChange: (String) -> Unit,
    onGoogleKeyChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var localOpenAIKey by remember { mutableStateOf(openaiKey) }
    var localAnthropicKey by remember { mutableStateOf(anthropicKey) }
    var localGoogleKey by remember { mutableStateOf(googleKey) }

    // Update local state when props change (e.g., when dialog reopens with new values)
    LaunchedEffect(openaiKey, anthropicKey, googleKey) {
        localOpenAIKey = openaiKey
        localAnthropicKey = anthropicKey
        localGoogleKey = googleKey
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
