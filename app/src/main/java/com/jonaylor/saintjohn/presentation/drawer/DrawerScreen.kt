package com.jonaylor.saintjohn.presentation.drawer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import com.jonaylor.saintjohn.domain.model.AppCategory
import com.jonaylor.saintjohn.domain.model.AppInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerScreen(
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    viewModel: DrawerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val pinnedApps = remember(uiState.categorizedApps) {
        uiState.categorizedApps.values.flatten().filter { it.isPinned }
    }
    val hasPinnedApps = pinnedApps.isNotEmpty()
    val bottomPadding = if (hasPinnedApps) 180.dp else 80.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .imePadding()
    ) {
        // Main content
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                if (searchQuery.isNotBlank()) {
                    // Show search results
                    SearchResults(
                        apps = uiState.filteredApps,
                        onAppClick = onAppClick,
                        onAppLongClick = onAppLongClick,
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = bottomPadding)
                    )
                } else {
                    // Show categorized apps
                    CategorizedAppList(
                        categorizedApps = uiState.categorizedApps,
                        onAppClick = onAppClick,
                        onAppLongClick = onAppLongClick,
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = bottomPadding)
                    )
                }
            }
        }

        // Bottom section with pinned apps and search
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            // Pinned apps bar
            if (hasPinnedApps) {
                PinnedAppsBar(
                    pinnedApps = pinnedApps,
                    onAppClick = onAppClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Search Bar at bottom
            SearchBar(
                query = searchQuery,
                onQueryChange = viewModel::updateSearchQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = {
            Text(
                "Search apps...",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.outline,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun CategorizedAppList(
    categorizedApps: Map<AppCategory, List<AppInfo>>,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    // State to track which categories are expanded (all collapsed by default)
    val expandedCategories = remember { mutableStateMapOf<AppCategory, Boolean>() }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
    ) {
        categorizedApps.forEach { (category, apps) ->
            if (apps.isNotEmpty()) {
                val isExpanded = expandedCategories[category] ?: false

                item(key = "header_$category") {
                    CategoryHeader(
                        category = category,
                        apps = apps,
                        isExpanded = isExpanded,
                        onToggle = {
                            expandedCategories[category] = !isExpanded
                        }
                    )
                }

                // Only show apps if category is expanded
                if (isExpanded) {
                    item(key = "grid_$category") {
                        AppGrid(
                            apps = apps,
                            category = category,
                            onAppClick = onAppClick,
                            onAppLongClick = onAppLongClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResults(
    apps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(apps, key = { "search_${it.packageName}" }) { app ->
            AppCard(
                app = app,
                onClick = { onAppClick(app) },
                onLongClick = { onAppLongClick(app) }
            )
        }
    }
}

@Composable
fun CategoryHeader(
    category: AppCategory,
    apps: List<AppInfo>,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "arrow_rotation"
    )

    val totalUsageTime = remember(apps) {
        apps.sumOf { it.usageTime }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: Category name and arrow
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 20.sp
            )

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(rotationAngle)
            )
        }

        // Right side: Usage time widget
        if (totalUsageTime > 0) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = formatUsageTime(totalUsageTime),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun AppGrid(
    apps: List<AppInfo>,
    category: AppCategory,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.heightIn(max = 2000.dp),
        userScrollEnabled = false
    ) {
        items(apps, key = { "${category.name}_${it.packageName}" }) { app ->
            AppCard(
                app = app,
                onClick = { onAppClick(app) },
                onLongClick = { onAppLongClick(app) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppCard(
    app: AppInfo,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App icon (greyscale)
            if (app.icon != null) {
                val greyscaleMatrix = ColorMatrix().apply {
                    setToSaturation(0f) // 0f = full greyscale
                }

                Image(
                    bitmap = app.icon.toBitmap().asImageBitmap(),
                    contentDescription = app.label,
                    modifier = Modifier.size(48.dp),
                    colorFilter = ColorFilter.colorMatrix(greyscaleMatrix)
                )
            } else {
                // Fallback to letter circle if no icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = app.label.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // App name
            Text(
                text = app.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 14.sp
            )

            // Usage time or lock indicator
            if (app.usageTime > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatUsageTime(app.usageTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            } else if (app.isLocked) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\uD83D\uDD12",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun PinnedAppsBar(
    pinnedApps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .padding(vertical = 16.dp)
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(pinnedApps.take(5), key = { "pinned_${it.packageName}" }) { app ->
                PinnedAppIcon(
                    app = app,
                    onClick = { onAppClick(app) }
                )
            }
        }
    }
}

@Composable
fun PinnedAppIcon(
    app: AppInfo,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        // App icon (greyscale)
        if (app.icon != null) {
            val greyscaleMatrix = ColorMatrix().apply {
                setToSaturation(0f)
            }

            Image(
                bitmap = app.icon.toBitmap().asImageBitmap(),
                contentDescription = app.label,
                modifier = Modifier.size(56.dp),
                colorFilter = ColorFilter.colorMatrix(greyscaleMatrix)
            )
        } else {
            // Fallback to letter circle
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = app.label.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = app.label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(64.dp)
        )
    }
}

private fun formatUsageTime(millis: Long): String {
    val hours = millis / (1000 * 60 * 60)
    val minutes = (millis % (1000 * 60 * 60)) / (1000 * 60)

    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "<1m"
    }
}
