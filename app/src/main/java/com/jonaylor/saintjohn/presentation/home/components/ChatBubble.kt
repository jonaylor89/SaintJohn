package com.jonaylor.saintjohn.presentation.home.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonaylor.saintjohn.domain.model.Message
import com.jonaylor.saintjohn.domain.model.MessageRole
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay

@Composable
fun ChatBubble(
    message: Message,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isAssistant = message.role == MessageRole.ASSISTANT

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (message.role == MessageRole.USER) {
            Arrangement.End
        } else {
            Arrangement.Start
        }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .widthIn(max = if (isAssistant) 320.dp else 280.dp)
                    .background(
                        color = when (message.role) {
                            MessageRole.USER -> MaterialTheme.colorScheme.primaryContainer
                            MessageRole.ASSISTANT -> if (message.isError) {
                                MaterialTheme.colorScheme.errorContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                            MessageRole.SYSTEM -> MaterialTheme.colorScheme.secondaryContainer
                        },
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (message.role == MessageRole.USER) 16.dp else 4.dp,
                            bottomEnd = if (message.role == MessageRole.USER) 4.dp else 16.dp
                        )
                    )
                    .padding(12.dp)
            ) {
                Column {
                    if (message.role == MessageRole.ASSISTANT && message.content.isEmpty() && !message.isError) {
                        ThinkingAnimation()
                    } else {
                        val textColor = when (message.role) {
                            MessageRole.USER -> MaterialTheme.colorScheme.onPrimaryContainer
                            MessageRole.ASSISTANT -> if (message.isError) {
                                MaterialTheme.colorScheme.onErrorContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                            MessageRole.SYSTEM -> MaterialTheme.colorScheme.onSecondaryContainer
                        }

                        if (isAssistant && !message.isError) {
                            MarkdownText(
                                markdown = message.content,
                                color = textColor
                            )
                        } else {
                            SelectionContainer {
                                Text(
                                    text = message.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = textColor,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (message.role == MessageRole.USER) {
                            Arrangement.End
                        } else {
                            Arrangement.SpaceBetween
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatTimestamp(message.timestamp),
                            style = MaterialTheme.typography.bodySmall,
                            color = when (message.role) {
                                MessageRole.USER -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                MessageRole.ASSISTANT -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                MessageRole.SYSTEM -> MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            },
                            fontSize = 10.sp
                        )

                        if (isAssistant && message.content.isNotEmpty() && !message.isError) {
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("message", message.content)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy message",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThinkingAnimation() {
    var visibleDots by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(400)
            visibleDots = (visibleDots + 1) % 4
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Thinking",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontSize = 14.sp
        )

        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .alpha(if (index < visibleDots) 1f else 0.3f)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        shape = CircleShape
                    )
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> {
            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
        else -> {
            val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}
