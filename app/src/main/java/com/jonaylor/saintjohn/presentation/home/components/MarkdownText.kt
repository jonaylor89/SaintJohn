package com.jonaylor.saintjohn.presentation.home.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.TextView
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    onLinkClick: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val textColor = color.toArgb()

    val markwon = remember(context) {
        Markwon.builder(context)
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TablePlugin.create(context))
            .usePlugin(TaskListPlugin.create(context))
            .build()
    }

    val content = markdown.trim()
    val codeBlocks = remember(content) { extractCodeBlocks(content) }

    if (codeBlocks.isEmpty()) {
        AndroidView(
            modifier = modifier,
            factory = { ctx ->
                TextView(ctx).apply {
                    setTextColor(textColor)
                    textSize = 14f
                    setTextIsSelectable(true)
                    setLineSpacing(0f, 1.2f)
                }
            },
            update = { textView ->
                markwon.setMarkdown(textView, content)
                textView.setTextColor(textColor)
            }
        )
    } else {
        Column(modifier = modifier) {
            var currentIndex = 0
            for (block in codeBlocks) {
                if (block.startIndex > currentIndex) {
                    val textBefore = content.substring(currentIndex, block.startIndex).trim()
                    if (textBefore.isNotEmpty()) {
                        AndroidView(
                            factory = { ctx ->
                                TextView(ctx).apply {
                                    setTextColor(textColor)
                                    textSize = 14f
                                    setTextIsSelectable(true)
                                    setLineSpacing(0f, 1.2f)
                                }
                            },
                            update = { textView ->
                                markwon.setMarkdown(textView, textBefore)
                                textView.setTextColor(textColor)
                            }
                        )
                    }
                }

                CodeBlock(
                    code = block.code,
                    language = block.language,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                currentIndex = block.endIndex
            }

            if (currentIndex < content.length) {
                val textAfter = content.substring(currentIndex).trim()
                if (textAfter.isNotEmpty()) {
                    AndroidView(
                        factory = { ctx ->
                            TextView(ctx).apply {
                                setTextColor(textColor)
                                textSize = 14f
                                setTextIsSelectable(true)
                                setLineSpacing(0f, 1.2f)
                            }
                        },
                        update = { textView ->
                            markwon.setMarkdown(textView, textAfter)
                            textView.setTextColor(textColor)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CodeBlock(
    code: String,
    language: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val backgroundColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val textColor = MaterialTheme.colorScheme.onSurface
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = language?.uppercase() ?: "CODE",
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
                fontWeight = FontWeight.Medium
            )

            IconButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("code", code)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy code",
                    tint = labelColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            Text(
                text = code,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                ),
                color = textColor
            )
        }
    }
}

private data class CodeBlockInfo(
    val code: String,
    val language: String?,
    val startIndex: Int,
    val endIndex: Int
)

private fun extractCodeBlocks(markdown: String): List<CodeBlockInfo> {
    val blocks = mutableListOf<CodeBlockInfo>()
    val pattern = Regex("```(\\w*)\\n([\\s\\S]*?)```")

    pattern.findAll(markdown).forEach { match ->
        val language = match.groupValues[1].takeIf { it.isNotEmpty() }
        val code = match.groupValues[2].trimEnd()
        blocks.add(
            CodeBlockInfo(
                code = code,
                language = language,
                startIndex = match.range.first,
                endIndex = match.range.last + 1
            )
        )
    }

    return blocks
}
