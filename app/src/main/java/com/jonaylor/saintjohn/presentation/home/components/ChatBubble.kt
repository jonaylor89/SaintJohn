package com.jonaylor.saintjohn.presentation.home.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import java.io.OutputStream
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonaylor.saintjohn.domain.model.Message
import com.jonaylor.saintjohn.domain.model.MessageImage
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
                    // Show thinking section for assistant messages with thinking content
                    if (isAssistant && !message.thinking.isNullOrEmpty()) {
                        ThinkingSection(
                            thinking = message.thinking,
                            thinkingSummary = message.thinkingSummary,
                            messageId = message.id
                        )
                        if (message.content.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    
                    if (message.role == MessageRole.ASSISTANT && message.content.isEmpty() && message.images.isEmpty() && !message.isError) {
                        // Show thinking animation only if there's no thinking content yet
                        if (message.thinking.isNullOrEmpty()) {
                            ThinkingAnimation()
                        } else {
                            // Thinking is happening but no answer yet - show indicator
                            ThinkingInProgressIndicator()
                        }
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

                        // Render images if present
                        if (message.images.isNotEmpty()) {
                            MessageImagesSection(images = message.images)
                            if (message.content.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        if (isAssistant && !message.isError && message.content.isNotEmpty()) {
                            MarkdownText(
                                markdown = message.content,
                                color = textColor
                            )
                        } else if (message.content.isNotEmpty()) {
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
private fun ThinkingSection(
    thinking: String,
    thinkingSummary: String?,
    messageId: Long
) {
    var isExpanded by rememberSaveable(messageId) { mutableStateOf(false) }
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        // Header row - always visible
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Reasoning",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Copy thinking button when expanded
                if (isExpanded) {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("thinking", thinking)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Reasoning copied", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(18.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy reasoning",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
                
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        // Preview text when collapsed
        if (!isExpanded) {
            val preview = thinkingSummary ?: thinking.take(80).replace("\n", " ")
            Text(
                text = if (preview.length >= 80) "$preview..." else preview,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        
        // Expanded content
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            SelectionContainer {
                Text(
                    text = thinking,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun ThinkingInProgressIndicator() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Psychology,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = "Generating response",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontSize = 12.sp
        )
        ThinkingDots()
    }
}

@Composable
private fun ThinkingDots() {
    var visibleDots by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(400)
            visibleDots = (visibleDots + 1) % 4
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .alpha(if (index < visibleDots) 1f else 0.3f)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        shape = CircleShape
                    )
            )
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

@Composable
private fun MessageImagesSection(images: List<MessageImage>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        images.forEach { image ->
            GeneratedImage(image = image)
        }
    }
}

@Composable
private fun GeneratedImage(image: MessageImage) {
    val bitmap = remember(image.data) {
        try {
            val bytes = Base64.decode(image.data, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            android.util.Log.e("ChatBubble", "Failed to decode image", e)
            null
        }
    }

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var showOptions by remember { mutableStateOf(false) }

    if (showOptions && bitmap != null) {
        AlertDialog(
            onDismissRequest = { showOptions = false },
            title = { Text("Image Options") },
            text = { Text("What would you like to do with this image?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        saveImageToGallery(context, bitmap)
                        showOptions = false
                    }
                ) {
                    Text("Save to Gallery")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        copyImageToClipboard(context, bitmap)
                        showOptions = false
                    }
                ) {
                    Text("Copy to Clipboard")
                }
            }
        )
    }

    bitmap?.let { bmp ->
        Image(
            bitmap = bmp.asImageBitmap(),
            contentDescription = "Generated image",
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(8.dp)
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showOptions = true
                        }
                    )
                },
            contentScale = ContentScale.Fit
        )
    } ?: Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Failed to load image",
            color = MaterialTheme.colorScheme.onErrorContainer,
            fontSize = 12.sp
        )
    }
}

private fun saveImageToGallery(context: Context, bitmap: Bitmap): Uri? {
    val filename = "SaintJohn_${System.currentTimeMillis()}.png"
    var fos: OutputStream? = null
    var imageUri: Uri? = null
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/SaintJohn")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
    }
    
    val resolver = context.contentResolver
    
    try {
        imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        imageUri?.let {
            fos = resolver.openOutputStream(it)
            if (fos != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos!!)
                fos!!.flush()
            }
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && imageUri != null) {
            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(imageUri, contentValues, null, null)
        }
        
        Toast.makeText(context, "Image saved to Gallery", Toast.LENGTH_SHORT).show()
        return imageUri
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
        return null
    } finally {
        fos?.close()
    }
}

private fun copyImageToClipboard(context: Context, bitmap: Bitmap) {
    val uri = saveImageToGallery(context, bitmap)
    if (uri != null) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newUri(context.contentResolver, "Image", uri)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Image copied to clipboard", Toast.LENGTH_SHORT).show()
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
